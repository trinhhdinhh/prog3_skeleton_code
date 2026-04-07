package Typecheck.Pass;

import Absyn.*;
import Typecheck.Types.*;
import Typecheck.SymbolTable.*;
import Typecheck.TypeCheckException;
import Typecheck.Types.Type;

import java.util.ArrayList;

// This pass implements the type rules.
// Some of the logic has been implemented for you in the Types.
// Check out the "canAccept" functions.
public class JudgementsPass extends ScopePass<Void> {

   // Tracks the return type of current function.
   private Type currentReturnType = null;

   public JudgementsPass(Scope s) {
      super(s);
   }

   // -----------------------------------------------------------------------
   // Var decl — check initializer type matches declared type
   // -----------------------------------------------------------------------
   @Override
   public Void visitVarDecl(VarDecl node) {
      super.visitVarDecl(node);
      if (node.init != null && !(node.init instanceof EmptyExp) && !(node.init instanceof ExpList && ((ExpList)node.init).list.isEmpty())) {
         Type declType = node.type.typeAnnotation;
         Type initType = node.init.typeAnnotation;

         // Block: var int x = &someNonIntTerm
         if (declType instanceof INT && !(declType instanceof POINTER)
                 && node.init instanceof UnaryExp
                 && ((UnaryExp) node.init).prefix.equals("&")
                 && initType instanceof POINTER
                 && !(((POINTER) initType).type instanceof INT)) {
            throw new TypeCheckException(
                    "type mismatch in variable declaration: cannot assign " +
                            initType + " to " + declType
            );
         }

         if (declType instanceof ARRAY && initType instanceof LIST) {
            LIST list = (LIST) initType;
            ARRAY arr = (ARRAY) declType;
            if (declType instanceof ARRAY && initType instanceof LIST) {
               if (!matchArray(declType, initType)) {
                  throw new TypeCheckException(
                          "array initializer does not match declared dimensions/type"
                  );
               }
            }
         } else if (!declType.canAccept(initType)) {
            throw new TypeCheckException(
                    "type mismatch in variable declaration: cannot assign " +
                            initType + " to " + declType
            );
         }
      }
      return null;
   }

   private boolean matchArray(Type expected, Type actual) {
      if (expected instanceof ARRAY) {
         if (!(actual instanceof LIST)) return false;

         LIST list = (LIST) actual;
         for (Type t : list.typelist) {
            if (!matchArray(((ARRAY) expected).type, t)) {
               return false;
            }
         }
         return true;
      } else {
         return expected.canAccept(actual);
      }
   }

   // -----------------------------------------------------------------------
   // Fun decl  — save/restore return type around the body
   // -----------------------------------------------------------------------
   @Override
   public Void visitFunDecl(FunDecl node) {
      Type savedReturnType = currentReturnType;
      currentReturnType = node.type.typeAnnotation;
      super.visitFunDecl(node); // ScopePass switches scope, then visits children
      currentReturnType = savedReturnType;
      return null;
   }

   // -----------------------------------------------------------------------
   // Return statement
   //   - void functions: no return statement with an expression allowed
   //   - non-void: expression type must match declared return type
   // -----------------------------------------------------------------------
   @Override
   public Void visitReturnStmt(ReturnStmt node) {
      super.visitReturnStmt(node); // visit children so typeAnnotation is set
      if (currentReturnType == null) {
         throw new TypeCheckException("return statement outside of function");
      }
      if (currentReturnType instanceof VOID) {
         if (node.expression != null) {
            throw new TypeCheckException("void function cant return a value");
         }
         return null;
      }
      Type exprType = (node.expression != null) ? node.expression.typeAnnotation : new VOID();
      if (!currentReturnType.canAccept(exprType)) {
         throw new TypeCheckException(
                 "type mismatch in return: expected " + currentReturnType +
                         " but got " + exprType
         );
      }
      return null;
   }

   // -----------------------------------------------------------------------
   // Assignment — LHS type must accept RHS type
   // -----------------------------------------------------------------------
   @Override
   public Void visitAssignExp(AssignExp node) {
      super.visitAssignExp(node);
      if (!node.left.typeAnnotation.canAccept(node.right.typeAnnotation)) {
         throw new TypeCheckException(
                 "type mismatch in assignment: cannot assign " +
                         node.right.typeAnnotation + " to " + node.left.typeAnnotation
         );
      }
      node.typeAnnotation = node.left.typeAnnotation;
      return null;
   }

   // -----------------------------------------------------------------------
   // Binary expression — arithmetic and comparisons require INT operands
   // -----------------------------------------------------------------------
   @Override
   public Void visitBinOp(BinOp node) {
      super.visitBinOp(node); // visit children first so operand types are set
      switch (node.oper) {
         case "+": case "-": case "*": case "/":
         case "==": case "!=": case "<": case ">": case "<=": case ">=":
            if (!new INT().canAccept(node.left.typeAnnotation)) {
               throw new TypeCheckException(
                       "binary '" + node.oper + "' requires INT left operand, got " +
                               node.left.typeAnnotation
               );
            }
            if (!new INT().canAccept(node.right.typeAnnotation)) {
               throw new TypeCheckException(
                       "binary '" + node.oper + "' requires INT right operand, got " +
                               node.right.typeAnnotation
               );
            }
            node.typeAnnotation = new INT();
            break;
         default:
            throw new TypeCheckException("unknown binary operator: " + node.oper);
      }
      return null;
   }

   // -----------------------------------------------------------------------
   // Function call — check arity and argument types, then set result type
   // -----------------------------------------------------------------------
   @Override
   public Void visitFunExp(FunExp node) {
      visit(node.params); // visit args only — node.name is a function name, not a variable
      String funName = ((ID) node.name).value;
      FunSymbol fun = currentscope.getFun(funName);
      if (fun == null) {
         throw new TypeCheckException("undefined function: " + funName);
      }

      ArrayList<Type> paramTypes = fun.params.typelist;
      int paramCount = paramTypes.size();
      int argCount   = (node.params != null) ? node.params.list.size() : 0;
      if (paramCount != argCount) {
         throw new TypeCheckException(
                 "wrong number of arguments for '" + funName +
                         "': expected " + paramCount + " but got " + argCount
         );
      }

      for (int i = 0; i < paramCount; i++) {
         Type expected = paramTypes.get(i);
         Type actual = node.params.list.get(i).typeAnnotation;
         if (!expected.canAccept(actual)) {
            throw new TypeCheckException(
                    "argument " + (i + 1) + " of '" + funName +
                            "': expected " + expected + " but got " + actual
            );
         }
      }

      node.typeAnnotation = fun.returnType;
      return null;
   }

   // -----------------------------------------------------------------------
   // If / While — condition must be a number
   // -----------------------------------------------------------------------
   @Override
   public Void visitIfStmt(IfStmt node) {
      super.visitIfStmt(node);
      if (!new INT().canAccept(node.expression.typeAnnotation)) {
         throw new TypeCheckException(
                 "if condition must be INT, got " + node.expression.typeAnnotation
         );
      }
      return null;
   }

   @Override
   public Void visitWhileStmt(WhileStmt node) {
      super.visitWhileStmt(node);
      if (!new INT().canAccept(node.expression.typeAnnotation)) {
         throw new TypeCheckException(
                 "while condition must be INT, got " + node.expression.typeAnnotation
         );
      }
      return null;
   }

   // -----------------------------------------------------------------------
   // Unary expression
   //   !  -  :  must be INT, result is INT
   //   &     : address-of any term to POINTER(T)
   //   *     : dereference — must specifically be POINTER, result is inner type
   // -----------------------------------------------------------------------
   @Override
   public Void visitUnaryExp(UnaryExp node) {
      super.visitUnaryExp(node);
      switch (node.prefix) {
         case "!":
         case "-":
            if (!(node.exp.typeAnnotation instanceof INT)) {
               throw new TypeCheckException(
                       "unary '" + node.prefix + "' requires INT operand, got " +
                               node.exp.typeAnnotation
               );
            }
            node.typeAnnotation = new INT();
            break;
         case "&": // address-of: any term → POINTER(T)
            node.typeAnnotation = new POINTER(node.exp.typeAnnotation);
            break;
         case "*": // dereference: must specifically be a POINTER
            if (node.exp.typeAnnotation == null || !(node.exp.typeAnnotation instanceof POINTER)) {
               throw new TypeCheckException(
                       "dereference '*' requires POINTER operand, got " +
                               node.exp.typeAnnotation
               );
            }
            node.typeAnnotation = ((POINTER) node.exp.typeAnnotation).type;
            break;
         default:
            throw new TypeCheckException("unknown unary operator: " + node.prefix);
      }
      return null;
   }

   // -----------------------------------------------------------------------
   // Array indexing — all indices must be INT, result is element type
   // -----------------------------------------------------------------------
   @Override
   public Void visitArrayExp(ArrayExp node) {
      super.visitArrayExp(node);
      // Check every index in the list is INT

      for (Exp idxExp : node.index_list.list) {
         if (!(idxExp.typeAnnotation instanceof INT)) {
            throw new TypeCheckException(
                    "array index must be INT, got " + idxExp.typeAnnotation
            );
         }
      }
      // Unwrap one level of ARRAY/LIST per index dimension
      Type t = node.name.typeAnnotation;
      int dimCount = node.index_list.list.size();
      for (int d = 0; d < dimCount; d++) {
         if (t instanceof ARRAY) {
            t = ((ARRAY) t).type;
         } else if (t instanceof LIST) {
            LIST list = (LIST) t;
            if (list.typelist.isEmpty()) {
               throw new TypeCheckException("cannot index into empty LIST");
            }
            t = list.typelist.get(0);
         } else {
            throw new TypeCheckException(
                    "subscript applied to non-array type: " + t
            );
         }
      }
      node.typeAnnotation = t;
      return null;
   }


   // -----------------------------------------------------------------------
   // Literals — annotate with their types
   // -----------------------------------------------------------------------
   @Override
   public Void visitDecLit(DecLit node) {
      node.typeAnnotation = new INT();
      return null;
   }

   @Override
   public Void visitStrLit(StrLit node) {
      node.typeAnnotation = new STRING();
      return null;
   }

   @Override
   public Void visitID(ID node) {
      // Look up the variable in scope to get its type
      VarSymbol var = currentscope.getVar(node.value);
      if (var != null) {
         node.typeAnnotation = var.type;
      } else {
         // Might be a function name used as expression — leave for visitFunExp
         // If truly undefined it will be caught there
      }
      return null;
   }


   // -----------------------------------------------------------------------
   // ExpList — annotate as a LIST built from element types
   // e.g. {1, "hello"} becomes LIST(INT, STRING)
   // -----------------------------------------------------------------------
   @Override
   public Void visitExpList(ExpList node) {
      super.visitExpList(node);
      ArrayList<Type> types = new ArrayList<>();
      int subListSize = -1;
      for (Exp e : node.list) {
         if (e.typeAnnotation instanceof LIST) {
            int sz = ((LIST) e.typeAnnotation).typelist.size();
            if (subListSize == -1) {
               subListSize = sz;
            } else if (sz != subListSize) {
               throw new TypeCheckException(
                       "jagged array initializer: all rows must have the same length"
               );
            }
         }
         types.add(e.typeAnnotation);
      }
      node.typeAnnotation = new LIST(types);
      return null;
   }
}
