package Typecheck.Pass;

import Typecheck.Types.*;
import Typecheck.SymbolTable.*;
import Typecheck.TypeCheckException;
import java.util.ArrayList;

public class FunAndVarScopePass extends ScopePass {

    public FunAndVarScopePass(Scope s) {
        super(s);
    }

    @Override
    public Void visitParameter(Absyn.Parameter node) {
        super.visitParameter(node);

        if (this.currentscope.hasLocalFun(node.name)) {
            throw new TypeCheckException("Tried to define var (" + node.name + ") but fun with same name already exists");
        }

        this.currentscope.addVar(node.name, new VarSymbol(node.name, node.type.typeAnnotation));
        return null;
    }

    @Override
    public Void visitFunDecl(Absyn.FunDecl node) {
        if (this.currentscope.hasVar(node.name)) {
            throw new TypeCheckException("Tried to define fun (" + node.name + ") but var with same name already exists");
        }

        ArrayList<Type> paramTypes = new ArrayList<>();
        for (Object obj : node.params.list) {
            Absyn.Parameter param = (Absyn.Parameter) obj;
            paramTypes.add(param.type.typeAnnotation);
        }

        LIST paramsList = new LIST(paramTypes);
        FunSymbol funSym = new FunSymbol(node.name, paramsList, node.type.typeAnnotation);
        this.currentscope.addFun(node.name, funSym);

        super.visitFunDecl(node);
        return null;
    }

    @Override
    public Void visitStructMember(Absyn.StructMember node) {
        super.visitStructMember(node);

        if (this.currentscope.hasLocalFun(node.name)) {
            throw new TypeCheckException("Tried to define var (" + node.name + ") but fun with same name already exists");
        }

        this.currentscope.addVar(node.name, new VarSymbol(node.name, node.type.typeAnnotation));
        return null;
    }

    @Override
    public Void visitUnionMember(Absyn.UnionMember node) {
        super.visitUnionMember(node);

        if (this.currentscope.hasLocalFun(node.name)) {
            throw new TypeCheckException("Tried to define var (" + node.name + ") but fun with same name already exists");
        }

        this.currentscope.addVar(node.name, new VarSymbol(node.name, node.type.typeAnnotation));
        return null;
    }

    @Override
    public Void visitVarDecl(Absyn.VarDecl node) {
        super.visitVarDecl(node);

        if (this.currentscope.hasLocalFun(node.name)) {
            throw new TypeCheckException("Tried to define var (" + node.name + ") but fun with same name already exists");
        }

        this.currentscope.addVar(node.name, new VarSymbol(node.name, node.type.typeAnnotation));
        return null;
    }
}