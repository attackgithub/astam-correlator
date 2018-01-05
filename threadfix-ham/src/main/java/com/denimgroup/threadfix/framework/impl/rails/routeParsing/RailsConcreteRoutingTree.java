////////////////////////////////////////////////////////////////////////
//
//     Copyright (C) 2017 Applied Visions - http://securedecisions.com
//
//     The contents of this file are subject to the Mozilla Public License
//     Version 2.0 (the "License"); you may not use this file except in
//     compliance with the License. You may obtain a copy of the License at
//     http://www.mozilla.org/MPL/
//
//     Software distributed under the License is distributed on an "AS IS"
//     basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
//     License for the specific language governing rights and limitations
//     under the License.
//
//     This material is based on research sponsored by the Department of Homeland
//     Security (DHS) Science and Technology Directorate, Cyber Security Division
//     (DHS S&T/CSD) via contract number HHSP233201600058C.
//
//     Contributor(s):
//              Secure Decisions, a division of Applied Visions, Inc
//
////////////////////////////////////////////////////////////////////////

package com.denimgroup.threadfix.framework.impl.rails.routeParsing;

import com.denimgroup.threadfix.framework.impl.rails.model.RailsRoutingEntry;

import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class RailsConcreteRoutingTree {
    RailsRoutingEntry rootEntry;
    ListIterator<RailsRoutingEntry> currentIterator = null;

    public RailsRoutingEntry getRootEntry() {
        return rootEntry;
    }

    public void setRootEntry(RailsRoutingEntry rootEntry) {
        this.rootEntry = rootEntry;
    }


    public void walkTree(RailsConcreteTreeVisitor visitor) {
        currentIterator = null;
        walkTree(rootEntry, visitor);
    }

    public void walkTree(RailsRoutingEntry startNode, RailsConcreteTreeVisitor visitor) {
        visitor.visitEntry(startNode, currentIterator);

        List<RailsRoutingEntry> children = startNode.getChildren();
        ListIterator<RailsRoutingEntry> iterator = children.listIterator();

        while (iterator.hasNext()) {
            RailsRoutingEntry node = iterator.next();
            currentIterator = iterator;
            walkTree(node, visitor);
        }
    }


    public <Type extends RailsRoutingEntry> Collection<Type> findEntriesOfType(final Class type) {
        final List<Type> result = list();

        walkTree(new RailsConcreteTreeVisitor()
        {
            @Override
            public void visitEntry(RailsRoutingEntry entry, ListIterator<RailsRoutingEntry> iterator) {
                if (type.isAssignableFrom(entry.getClass())) {
                    result.add((Type) entry);
                }
            }
        });

        return result;
    }
}
