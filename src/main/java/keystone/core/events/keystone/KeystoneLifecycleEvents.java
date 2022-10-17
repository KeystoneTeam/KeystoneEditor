package keystone.core.events.keystone;

import keystone.core.modules.schematic_import.ImportBoundingBox;
import keystone.core.modules.selection.SelectionBoundingBox;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.world.ClientWorld;

import java.util.List;
import java.util.Properties;

public final class KeystoneLifecycleEvents
{
    //region World Events
    public static final Event<OpenWorld> OPEN_WORLD = EventFactory.createArrayBacked(OpenWorld.class, listeners -> world ->
    {
        for (final OpenWorld listener : listeners) listener.join(world);
    });
    public static final Event<CloseWorld> CLOSE_WORLD = EventFactory.createArrayBacked(CloseWorld.class, listeners -> () ->
    {
        for (final CloseWorld listener : listeners) listener.leave();
    });
    
    public interface OpenWorld
    {
        void join(ClientWorld world);
    }
    public interface CloseWorld
    {
        void leave();
    }
    //endregion
    //region Session Events
    public static final Event<RepairSession> REPAIR_SESSION = EventFactory.createArrayBacked(RepairSession.class, listeners -> (sessionInfo) ->
    {
        for (final RepairSession listener : listeners) listener.repairSession(sessionInfo);
    });
    public static final Event<SaveSessionInfo> SAVE_SESSION_INFO = EventFactory.createArrayBacked(SaveSessionInfo.class, listeners -> (sessionInfo) ->
    {
        for (final SaveSessionInfo listener : listeners) listener.saveSessionInfo(sessionInfo);
    });
    public static final Event<CommitSession> COMMIT_SESSION = EventFactory.createArrayBacked(CommitSession.class, listeners -> () ->
    {
        for (final CommitSession listener : listeners) listener.commitSession();
    });
    public static final Event<RevertSession> REVERT_SESSION = EventFactory.createArrayBacked(RevertSession.class, listeners -> () ->
    {
        for (final RevertSession listener : listeners) listener.revertSession();
    });
    
    public interface RepairSession
    {
        void repairSession(Properties sessionInfo);
    }
    public interface SaveSessionInfo
    {
        void saveSessionInfo(Properties sessionInfo);
    }
    public interface CommitSession
    {
        void commitSession();
    }
    public interface RevertSession
    {
        void revertSession();
    }
    //endregion
    //region Misc Events
    public static final Event<SelectionChanged> SELECTION_CHANGED = EventFactory.createArrayBacked(SelectionChanged.class, listeners -> (selectionBoxes, createdSelection, createHistoryEntry) ->
    {
        for (final SelectionChanged listener : listeners) listener.selectionChanged(selectionBoxes, createdSelection, createHistoryEntry);
    });
    public static final Event<ImportsChanged> IMPORTS_CHANGED = EventFactory.createArrayBacked(ImportsChanged.class, listeners -> (importBoxes, createHistoryEntry) ->
    {
        for (final ImportsChanged listener : listeners) listener.importsChanged(importBoxes, createHistoryEntry);
    });
    
    public interface SelectionChanged
    {
        void selectionChanged(List<SelectionBoundingBox> selectionBoxes, boolean createdSelection, boolean createHistoryEntry);
    }
    public interface ImportsChanged
    {
        void importsChanged(List<ImportBoundingBox> importBoxes, boolean createHistoryEntry);
    }
    //endregion
}
