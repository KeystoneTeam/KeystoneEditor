package keystone.core;

public record FeatureSupportFlags(
        boolean sessions
)
{
    public static class Builder
    {
        private boolean sessions = true;
        
        public Builder disableSessions() { this.sessions = false; return this; }
        
        public FeatureSupportFlags build()
        {
            return new FeatureSupportFlags(sessions);
        }
    }
    
    public static final FeatureSupportFlags ALL_FEATURES = new FeatureSupportFlags(true);
    public static final FeatureSupportFlags NO_FEATURES = new FeatureSupportFlags(false);
    
    public static Builder builder() { return new Builder(); }
}
