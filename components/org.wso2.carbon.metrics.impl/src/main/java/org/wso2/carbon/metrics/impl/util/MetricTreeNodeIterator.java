/*
 * Copyright 2015 WSO2 Inc. (http://wso2.org)
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
 */

package org.wso2.carbon.metrics.impl.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class MetricTreeNodeIterator implements Iterator<MetricTreeNode> {

    private MetricTreeNode metricTreeNode;
    private ProcessStages doNext;
    private MetricTreeNode next;
    private Iterator<MetricTreeNode> childrenCurNodeIter;
    private Iterator<MetricTreeNode> childrenSubNodeIter;

    public MetricTreeNodeIterator(MetricTreeNode metricTreeNode) {
        this.metricTreeNode = metricTreeNode;
        this.doNext = ProcessStages.ProcessParent;
        this.childrenCurNodeIter = metricTreeNode.children.iterator();
    }

    @Override
    public boolean hasNext() {
        if (this.doNext == ProcessStages.ProcessParent) {
            this.next = this.metricTreeNode;
            this.doNext = ProcessStages.ProcessChildCurNode;
            return true;
        }

        if (this.doNext == ProcessStages.ProcessChildCurNode) {
            if (childrenCurNodeIter.hasNext()) {
                MetricTreeNode childDirect = childrenCurNodeIter.next();
                childrenSubNodeIter = childDirect.iterator();
                this.doNext = ProcessStages.ProcessChildSubNode;
                return hasNext();
            } else {
                this.doNext = null;
                return false;
            }
        }

        if (this.doNext == ProcessStages.ProcessChildSubNode) {
            if (childrenSubNodeIter.hasNext()) {
                this.next = childrenSubNodeIter.next();
                return true;
            } else {
                this.next = null;
                this.doNext = ProcessStages.ProcessChildCurNode;
                return hasNext();
            }
        }
        return false;
    }

    @Override
    public MetricTreeNode next() {
        if (hasNext()) {
            return this.next;
        } else {
            throw new NoSuchElementException();
        }
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    enum ProcessStages {
        ProcessParent, ProcessChildCurNode, ProcessChildSubNode
    }
}