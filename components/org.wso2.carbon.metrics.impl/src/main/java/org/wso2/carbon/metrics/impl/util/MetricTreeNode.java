/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.metrics.impl.util;

import org.wso2.carbon.metrics.impl.AbstractMetric;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class MetricTreeNode implements Iterable<MetricTreeNode> {

    public String name;
    private ConcurrentMap<String, AbstractMetric> metrics;
    public MetricTreeNode parent;
    public List<MetricTreeNode> children;
    private ConcurrentMap<String, MetricTreeNode> elementsIndex;

    public MetricTreeNode(String name) {
        this.name = name;
        this.metrics = new ConcurrentHashMap<String, AbstractMetric>();
        this.children = new LinkedList<MetricTreeNode>();
        this.elementsIndex = new ConcurrentHashMap<String, MetricTreeNode>();
        this.elementsIndex.put(name, this);
    }

    public boolean isRoot() {
        return parent == null;
    }

    public boolean isLeaf() {
        return children.size() == 0;
    }

    public MetricTreeNode addChild(String name) {
        MetricTreeNode childNode = this.getNodeByName(name);
        if (childNode == null) {
            childNode = new MetricTreeNode(name);
            childNode.parent = this;
            this.children.add(childNode);
            this.registerChildForSearch(name, childNode);
        }
        return childNode;
    }

    public int getLevel() {
        if (parent != null) {
            return parent.getLevel() + 1;
        } else {
            return 0;
        }
    }

    private void registerChildForSearch(String name, MetricTreeNode node) {
        elementsIndex.put(node.name, node);
        if (parent != null) {
            parent.registerChildForSearch(name, node);
        }
    }

    public MetricTreeNode getNodeByName(String name) {
        if (name != null) {
            return this.elementsIndex.get(name);
        } else {
            return null;
        }
    }

    public AbstractMetric addMetric(String statName, AbstractMetric metric) {
        return metrics.putIfAbsent(statName, metric);
    }

    public ConcurrentMap<String, AbstractMetric> getMetrics() {
        return metrics;
    }

    @Override
    public String toString() {
        return "{ name: " + name + ", metrics count: " + metrics.size() + ", immediate children: " + children.size() + " }";
    }

    @Override
    public Iterator<MetricTreeNode> iterator() {
        return new MetricTreeNodeIterator(this);
    }
}