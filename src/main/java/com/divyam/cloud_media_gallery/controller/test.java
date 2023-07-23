package com.divyam.cloud_media_gallery.controller;

public class test {

    public void main(String[] args) {
        Tag tag = new Tag("abc");
        System.out.println(tag.getValue());
    }

    class Tag implements Comparable<Tag>{
        private String value;

        public Tag(String value) {
            this.value = value;
        }

        @Override
        public int compareTo(Tag o) {
            return o.getValue().compareTo(this.value);
        }

        public String getValue(){
            return this.value;
        }

        public void setValue(String value){
            this.value = value;
        }

    }
}