package keystone.core.modules.filter.execution;

import keystone.api.filters.FilterExecutionSettings;
import keystone.api.filters.KeystoneFilter;

public interface IFilterThread
{
    KeystoneFilter getFilter();
    FilterExecutor getExecutor();
    FilterExecutionSettings getSettings();
}
