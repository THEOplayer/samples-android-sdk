package com.theoplayer.demo.simpleott.datamodel;

public class SimpleOTTConfiguration {

    public Config config;

    public static class Config {
        public Live live;
        public OnDemand onDemand;
        public Offline offline;
    }

    public static class Live {
        public AssetItem[] channels;
    }

    public static class OnDemand {
        public  AssetItem[] vods;
    }

    public static class Offline {
        public AssetItem[] vods;
    }



}
