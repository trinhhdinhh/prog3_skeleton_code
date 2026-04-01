package Typecheck.Pass;
import Typecheck.SymbolTable.*;
import Typecheck.TypeCheckException;
import Absyn.*;

public class CreateScopePass extends Pass<Void> {

   protected Scope currentscope;
   public Scope globalscope;

   public CreateScopePass() {
      this.globalscope = new Scope();
      this.currentscope = globalscope;
   }
// Hint: Functions introduce a new nested scope.
// 1. Create a new Scope whose parent is the current scope.
// 2. Temporarily switch currentscope to this new scope.
// 3. Visit the function type, parameters, and body.
// 4. Store the resulting scope in node.scope.
// 5. Restore the previous scope.
   @Override
   public Void visitFunDecl(FunDecl node) {
      Scope newScope = new Scope(currentscope);
      node.scope = newScope;
      Scope saved = currentscope;
      currentscope = newScope;
      super.visitFunDecl(node);
      currentscope = saved;
      return null;
   }

   @Override
   public Void visitStructDecl(StructDecl node) {
      Scope newScope = new Scope(currentscope);
      node.scope = newScope;
      Scope saved = currentscope;
      currentscope = newScope;
      super.visitStructDecl(node);
      currentscope = saved;
      return null;
   }

   @Override
   public Void visitUnionDecl(UnionDecl node) {
      Scope newScope = new Scope(currentscope);
      node.scope = newScope;
      Scope saved = currentscope;
      currentscope = newScope;
      super.visitUnionDecl(node);
      currentscope = saved;
      return null;
   }

   @Override
   public Void visitIfStmt(IfStmt node) {
      Scope newScope = new Scope(currentscope);
      node.scope = newScope;
      Scope saved = currentscope;
      currentscope = newScope;
      super.visitIfStmt(node);
      currentscope = saved;
      return null;
   }

   @Override
   public Void visitWhileStmt(WhileStmt node) {
      Scope newScope = new Scope(currentscope);
      node.scope = newScope;
      Scope saved = currentscope;
      currentscope = newScope;
      super.visitWhileStmt(node);
      currentscope = saved;
      return null;
   }

}
