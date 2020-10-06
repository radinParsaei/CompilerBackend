import java.util.HashMap;

public class Data implements java.io.Serializable {
    transient private HashMap<String, ValueBase> variables = SyntaxTree.getVariables();
    transient private HashMap<String, ProgramBase> functions = SyntaxTree.getFunctions();
    transient private boolean isBreaked = false;
    transient private ValueBase returnedData = null;

    public ValueBase getReturnedData() {
        return returnedData;
    }

    public void setReturnedData(ValueBase returnedData) {
        this.returnedData = returnedData;
    }

    public boolean isBreaked() {
        return isBreaked;
    }

    public void setBreaked(boolean breaked) {
        isBreaked = breaked;
    }

    public Data setVariables(HashMap<String, ValueBase> variables) {
        this.variables = variables;
        return this;
    }

    public Data setFunctions(HashMap<String, ProgramBase> functions) {
        this.functions = functions;
        return this;
    }

    public HashMap<String, ValueBase> getVariables() {
        if (variables == null) {
            variables = SyntaxTree.getVariables();
        }
        return variables;
    }
    public HashMap<String, ProgramBase> getFunctions() {
        if (functions == null) {
            functions = SyntaxTree.getFunctions();
        }
        return functions;
    }
}
