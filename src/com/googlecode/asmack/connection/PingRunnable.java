/*
 * Licensed under Apache License, Version 2.0 or LGPL 2.1, at your option.
 * --
 *
 * Copyright 2010 Rene Treffer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * --
 *
 * Copyright (C) 2010 Rene Treffer
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA
 */

package com.googlecode.asmack.connection;

import com.googlecode.asmack.Stanza;
import com.googlecode.asmack.XmppException;

/**
 * Runnable to execute ping request in the background.
 */
public class PingRunnable implements Runnable {

    /**
     * The connection to use for ping.
     */
    private final Connection connection;

    /**
     * Create a new ping runnable bound to a given output connection.
     * @param connection The ping output connection.
     */
    public PingRunnable(Connection connection) {
        this.connection = connection;
    }

    /**
     * Execute the ping on the given connection.
     */
    @Override
    public void run() {
        Stanza stanza = new Stanza(
                "iq",
                "",
                "",
                "<iq from='" +
                connection.getResourceJid() +
                "' id='ping_" +
                Long.toHexString((int)(Integer.MAX_VALUE * Math.random()))
                + "'><ping xmlns='urn:xmpp:ping'/></iq>",
                null
        );
        try {
            connection.send(stanza);
        } catch (XmppException e) {
            /* PING is non critical */
        }
    }

}
