package Typecheck.Pass;
import Typecheck.Types.*;
import Typecheck.SymbolTable.*;
import java.util.ArrayList;

public class TypeScopePass extends ScopePass<Void> {

   public TypeScopePass(Scope s) {
      super(s);
   }
// Hint: Structs define a new type from their member types.
// 1. Visit the body so member types are fully resolved.
// 2. Collect each member's typeAnnotation.
// 3. Build a LIST type from them.
// 4. Register the struct name in the current scope.
   @Override
   public Void visitStructDecl(Absyn.StructDecl node) {
      Scope prevScope = currentscope;
      currentscope = node.scope; // switch to struct scope

      ArrayList<Type> memberTypes = new ArrayList<>();
      for (Absyn.Decl member : node.body.list) {
         member.accept(this);                     // visit to resolve typeAnnotation
         memberTypes.add(member.typeAnnotation); // collect typeAnnotation
      }

      Type structType = new LIST(memberTypes);

      prevScope.addType(node.name, new TypeSymbol(node.name, structType));
      currentscope = prevScope;
      return null;
   }
// Hint: Unions define a type that can be any of their member types.
// 1. Visit the body so member types are resolved.
// 2. Collect each member's typeAnnotation.
// 3. Build an OR type from them.
// 4. Register the union name in the current scope.
   @Override
	public Void visitUnionDecl(Absyn.UnionDecl node) {
      Scope prevScope = currentscope;
      currentscope = node.scope;
      
      ArrayList<Type> memberTypes = new ArrayList<>();
      for (Absyn.Decl member : node.body.list) {
         member.accept(this);
         memberTypes.add(member.typeAnnotation);
      }

      Type unionType = new OR(memberTypes);
      prevScope.addType(node.name, new TypeSymbol(node.name, unionType));
      currentscope = prevScope;
		return null;
   }
// Hint: Typedef introduces a new name for an existing type.
// Visit the type first, then register the alias in the current scope.
   @Override
	public Void visitTypedef(Absyn.Typedef node) {
      node.type.accept(this);
      currentscope.addType(node.name, new TypeSymbol(node.name, node.typeAnnotation));
		return null;
	}
// Hint: Replace ALIAS types with their real definition.
// Remember that Types can be nested (IE ARRAY(ARRAY(ARRAY(...))) )
// Traverse the whole type to search for Aliases. Once an alias is found,
// look up the type of the alias in the symbol table.
    // This is a function I found helpful to implement. If you have a solution
    // in mind that does not include a helper function, then feel free to ignore
   private Type resolveAlias(Type type) {
      if (type instanceof ALIAS) {
         ALIAS alias = (ALIAS) type;
         TypeSymbol ts = currentscope.getType(alias.name);
         if (ts == null) {
               throw new RuntimeException("Undefined type alias: " + alias.name);
         }
         return resolveAlias(ts.type);
      } 
      else if (type instanceof LIST) {
         LIST listType = (LIST) type;
         ArrayList<Type> resolvedMembers = new ArrayList<>();
         for (Type t : listType.typelist) {
               resolvedMembers.add(resolveAlias(t));
         }
         return new LIST(resolvedMembers);
      } 
      else if (type instanceof OR) {
         OR orType = (OR) type;
         ArrayList<Type> resolvedMembers = new ArrayList<>();
         for (Type t : orType.options) {
               resolvedMembers.add(resolveAlias(t));
         }
         return new OR(resolvedMembers);
      }
      return type;
   }


// Hint: Visit the brackets and resolve the alias to a type (if the typeAnnotation contains ALIAS)
   @Override
   public Void visitType(Absyn.Type node) {
      super.visitType(node); // visit brackets/sub-types first
      node.typeAnnotation = resolveAlias(node.typeAnnotation); // resolve alias if present
      return null;
   }

}
