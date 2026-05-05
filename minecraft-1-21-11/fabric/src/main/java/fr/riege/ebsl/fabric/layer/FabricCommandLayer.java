package fr.riege.ebsl.fabric.layer;

import fr.riege.ebsl.common.layer.ICommandLayer;
import java.util.List;

public class FabricCommandLayer implements ICommandLayer {
    @Override public void register(String name, String description, CommandHandler handler) { throw new UnsupportedOperationException("TODO"); }
    @Override public void print(String message) { throw new UnsupportedOperationException("TODO"); }
    @Override public void printError(String message) { throw new UnsupportedOperationException("TODO"); }
    @Override public void printSuccess(String message) { throw new UnsupportedOperationException("TODO"); }
    @Override public List<String> getSuggestions(String input) { throw new UnsupportedOperationException("TODO"); }
}
