package Typecheck.Pass;
import Absyn.*;
import Typecheck.SymbolTable.*;

public class ScopePass<T> extends Pass<T> {

	protected Scope currentscope;
	protected T defaultReturn = null;

	// Hint: Save scope → switch to node.scope → visit children → restore scope.

	public ScopePass(Scope s) {
		this.currentscope = s;
	}

	@Override
	public T visitFunDecl(FunDecl node) {
		Scope saved = currentscope;
		currentscope = node.scope;
		super.visitFunDecl(node);
		currentscope = saved;
		return defaultReturn;
	}

	@Override
	public T visitStructDecl(StructDecl node) {
		Scope saved = currentscope;
		currentscope = node.scope;
		super.visitStructDecl(node);
		currentscope = saved;
		return defaultReturn;
	}

	@Override
	public T visitUnionDecl(UnionDecl node) {
		Scope saved = currentscope;
		currentscope = node.scope;
		super.visitUnionDecl(node);
		currentscope = saved;
		return defaultReturn;
	}

	@Override
	public T visitIfStmt(IfStmt node) {
		Scope saved = currentscope;
		currentscope = node.scope;
		super.visitIfStmt(node);
		currentscope = saved;
		return defaultReturn;
	}

	@Override
	public T visitWhileStmt(WhileStmt node) {
		Scope saved = currentscope;
		currentscope = node.scope;
		super.visitWhileStmt(node);
		currentscope = saved;
		return defaultReturn;
	}

}