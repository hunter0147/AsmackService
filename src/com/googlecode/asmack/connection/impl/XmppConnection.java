package com.googlecode.asmack.connection.impl;

import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.SRVRecord;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

import com.googlecode.asmack.StanzaSink;
import com.googlecode.asmack.XMPPUtils;
import com.googlecode.asmack.XmppAccount;
import com.googlecode.asmack.XmppException;

/**
 * Xmpp compliant connection, resolving the XMPP server via DNS/SRV lookups.
 */
public class XmppConnection extends TcpConnection {

    /**
     * The initial xmpp domain.
     */
    private String xmppDomain;

    /**
     * Create a new xmpp connection object.
     * @param account XmppAccount The underlying xmpp account.
     */
    public XmppConnection(XmppAccount account) {
        super(account);
        xmppDomain = XMPPUtils.getDomain(account.getJid());
    }

    /**
     * Connect to the xmpp server associated with the user domain. This
     * method starts the DNS/SRV and TCP connect process, which in turn
     * starts the feature negotiation and authentification process.
     * @param sink StanzaSink The final stanza sink for incoming stanzas.
     * @throws XmppException In case of an xmpp error.
     * @see com.googlecode.asmack.connection.Connection#connect(StanzaSink)
     */
    @Override
    public void connect(StanzaSink sink) throws XmppException {
        // resolve connection string
        String[] resolvedXMPPDomain = resolveXMPPDomain(xmppDomain);
        account.setConnection("tcp:" + resolvedXMPPDomain[0] + ":" + resolvedXMPPDomain[1]);
        // handle by parent
        super.connect(sink);
    }

    /**
     * Tries to resolve the SRV record for a given domain.
     * @param domain String The target domain to check.
     * @return String[] A host/port pair.
     */
    private static String[] resolveSRV(String domain) {
        String bestHost = null;
        int bestPort = -1;
        int bestPriority = Integer.MAX_VALUE;
        int bestWeight = 0;
        Lookup lookup;
        try {
            lookup = new Lookup(domain, Type.SRV);
            Record recs[] = lookup.run();
            if (recs == null) {
                return null;
            }
            for (Record rec : recs) {
                SRVRecord record = (SRVRecord) rec;
                if (record != null && record.getTarget() != null) {
                    int weight = (int) (record.getWeight() * record.getWeight() * Math.random());
                    if (record.getPriority() < bestPriority) {
                        bestPriority = record.getPriority();
                        bestWeight = weight;
                        bestHost = record.getTarget().toString();
                        bestPort = record.getPort();
                    } else if (record.getPriority() == bestPriority) {
                        if (weight > bestWeight) {
                            bestPriority = record.getPriority();
                            bestWeight = weight;
                            bestHost = record.getTarget().toString();
                            bestPort = record.getPort();
                        }
                    }
                }
        }
        } catch (TextParseException e) {
        } catch (NullPointerException e) {
                        }
        if (bestHost == null) {
            return null;
        }
        // Host entries in DNS should end with a ".".
        if (bestHost.endsWith(".")) {
            bestHost = bestHost.substring(0, bestHost.length() - 1);
        }
        return new String[]{bestHost, Integer.toString(bestPort)};
    }

    /**
     * Resolve an xmpp DNS/SRV record for a given domain. This method
     * is roughly equivalent to resolving _xmpp-client._tcp.domain.tld and
     * _jabber._tcp.domain.tld.
     *
     * @param domain String The target domain string.
     * @return String[] A host/port pair.
     */
    public static String[] resolveXMPPDomain(String domain) {
        String result[] = resolveSRV("_xmpp-client._tcp." + domain);
        if (result == null) {
            result = resolveSRV("_jabber._tcp." + domain);
        }
        if (result == null) {
            result = new String[]{domain, "5222"};
        }
        return result;
    }

}
