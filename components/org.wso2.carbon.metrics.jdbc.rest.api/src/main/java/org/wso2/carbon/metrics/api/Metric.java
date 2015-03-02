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
package org.wso2.carbon.metrics.api;

/**
 * Description about Metric
 */
public class Metric {

    private byte leaf;
    
    private Object context;
    
    private String text;
    
    private String id;
    
    private byte allowChildren;
    
    private byte expandable;
    
    public Metric() {
    }

    public byte getLeaf() {
        return leaf;
    }

    public void setLeaf(byte leaf) {
        this.leaf = leaf;
    }

    public Object getContext() {
        return context;
    }

    public void setContext(Object context) {
        this.context = context;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public byte getAllowChildren() {
        return allowChildren;
    }

    public void setAllowChildren(byte allowChildren) {
        this.allowChildren = allowChildren;
    }

    public byte getExpandable() {
        return expandable;
    }

    public void setExpandable(byte expandable) {
        this.expandable = expandable;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof Metric))
            return false;
        Metric other = (Metric) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }
    
}
