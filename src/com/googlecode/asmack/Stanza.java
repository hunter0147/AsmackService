package com.googlecode.asmack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

/**
 * A stanza (xmpp stream fragment). Name and namespace have to be consistent
 * with the attached xml.
 */
public class Stanza implements Parcelable {

    /**
     * The complete xml fragment.
     */
    private String xml;

    /**
     * The name of the root tag.
     */
    private String name;

    /**
     * The namespace of the root tag.
     */
    private String namespace;

    /**
     * The jid used for sending/receiving.
     */
    private String via;

    /**
     * A list of XML attributes
     */
    private ArrayList<Attribute> attributes;

    /**
     * Create a new stanza with the given values. Name and namespace have to
     * correlate with the root element of the xml. The null namespace is mapped
     * to the empty namespace ("").
     * @param name The root element name.
     * @param namespace The root element namespace.
     * @param via The jid for receiving/sending.
     * @param xml The full stanza xml.
     * @param attributes The root element attributes, may be encoded into xml.
     */
    public Stanza(
        String name,
        String namespace, 
        String via,
        String xml,
        Collection<Attribute> attributes
    ) {
        if (namespace == null) {
            // kXML: namespace null and "" are different
            // so we should settle for one and only one.
            // "" is what you get when you use the pull parser
            namespace = "";
        }
        this.xml = xml;
        this.name = name;
        this.namespace = namespace;
        this.via = via;
        if (attributes == null) {
            this.attributes = new ArrayList<Attribute>(4);
        } else {
            this.attributes = new ArrayList<Attribute>(attributes);
        }
    }

    /**
     * Generate a stanza from a parcel.
     * @param source The parcel to read.
     */
    public Stanza(Parcel source) {
        readFromParcel(source);
    }

    /**
     * Add a specific attribute. Adding mutliple attributes of the same name
     * and namespace may have unpredicted results.
     * @param attr The attribute to add.
     */
    public void addAttribute(Attribute attr) {
        attributes.add(attr);
    }

    /**
     * Retrieve a readonly list of all attributes.
     * @return A unmodifiable list of all attributes.
     */
    public List<Attribute> getAttributes() {
        return Collections.unmodifiableList(attributes);
    }

    /**
     * Retrieve the attribute of the given name and empty namespace prefix.
     * @param name The attribute name to scan for.
     * @return The attribute with the given name or null.
     */
    public Attribute getAttribute(String name) {
        return getAttribute(name, "");
    }

    /**
     * Retrieve the attribute matching the givrn name and namespace.
     * @param name The name of the attribute in question.
     * @param namespace The namespace of the attribute in question.
     * @return An attribute with the give name and namespace or null.
     */
    public Attribute getAttribute(final String name, final String namespace) {
        if (namespace == null) {
            for (Attribute attr: attributes) {
                if (attr.getName().equals(name)) {
                    return attr;
                }
            }
            return null;
        }
        for (Attribute attr: attributes) {
            if (!attr.getName().equals(name)) {
                continue;
            }
            if (namespace.equals(attr.getNamespace())) {
                return attr;
            }
        }
        return null;
    }

    /**
     * Retrieve the stanza as a xml string.
     * @return The stanza xml as string.
     */
    public String getXml() {
        return xml;
    }

    /**
     * Retrieve the name of the root tag.
     * @return The name of the root tag.
     */
    public String getName() {
        return name;
    }

    /**
     * Retrieve the namespace of the root element.
     * @return The namespace of the root element.
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * Retrieve the connection jid for transfer of this stanza.
     * @return The jid of the connection used for sending/receiving this stanza.
     */
    public String getVia() {
        return via;
    }

    /**
     * Set the stanza source.
     * @param via The jid of the connection used for receiving.
     */
    public void setVia(String via) {
        this.via = via;
    }

    /**
     * Try to transform
     * @return A new DOM representation of the xml.
     * @throws XmppMalformedException
     */
    public Node getDocumentNode() throws XmppMalformedException
    {
        try {
            return XMLUtils.getDocumentNode(xml);
        } catch (SAXException e) {
            Log.e("ASMACK", "PLEASE REPORT", e);
            Log.e("ASMACK", "STANZA: " + xml);
            throw new XmppMalformedException("please report xml", e);
        } catch (IllegalStateException e) {
            Log.e("ASMACK", "PLEASE REPORT", e);
            Log.e("ASMACK", "STANZA: " + xml);
            throw new XmppMalformedException("please report xml", e);
        }
    }

    /**
     * Retrieve the special flags of this parcelable. None.
     * @return 0. (No special flags)
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Persist the Stanza to a Parcel.
     * @param dest The target parcel.
     * @param flags Ignored
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(namespace);
        dest.writeString(via);
        dest.writeInt(attributes.size());
        for (Attribute attribute: attributes) {
            dest.writeString(attribute.getName());
            dest.writeString(attribute.getNamespace());
            dest.writeString(attribute.getValue());
        }
        dest.writeString(xml);
    }

    /**
     * Initialize all values of this Stanza from a pacel. 
     * @param source The parcel containing all stanza data.
     */
    public void readFromParcel(Parcel source) {
        name = source.readString();
        namespace = source.readString();
        via = source.readString();
        int attributeCount = source.readInt();
        if (attributeCount > 0) {
            attributes = new ArrayList<Attribute>(attributeCount + 1);
            for (int i = 0; i < attributeCount; i++) {
                Attribute attribute = new Attribute(
                        source.readString(),
                        source.readString(),
                        source.readString());
                attributes.add(attribute);
            }
        } else {
            attributes = new ArrayList<Attribute>(0);
        }
        xml = source.readString();
    }

    /**
     * A Parcelable.Creator for reading stanza objects. Named CREATOR as
     * required by the parcelable convention.
     */
    public final static Parcelable.Creator<Stanza> CREATOR = new Creator<Stanza>() {

        /**
         * Create a new Stanza array of the given size.
         * @param size The size of the returned array.
         * @return A new Stanza array of length size.
         */
        @Override
        public Stanza[] newArray(int size) {
            return new Stanza[size];
        }

        /**
         * Create a new Stanza with the values of the source parcel. This
         * method delegates to the Stanza(Parcel) constructor.
         * @param source The source parcel.
         */
        @Override
        public Stanza createFromParcel(Parcel source) {
            return new Stanza(source);
        }

    };

}
