# Program 3 — Type Checker

A type checker for a custom programming language, implemented in Java. It takes a source file as input, parses it into an AST, and runs a series of passes to verify that the program is type-correct.

## How It Works

The type checker runs 5 passes over the AST in order:

1. **TypeAnnotationPass** — Annotates type nodes with internal type representations
2. **CreateScopePass** — Builds the scope tree by creating a new scope for every block (functions, structs, unions, if/while statements)
3. **TypeScopePass** — Registers type declarations (structs, unions, typedefs) into the scope
4. **FunAndVarScopePass** — Registers function and variable declarations into the scope
5. **JudgementsPass** — Enforces the actual type rules (e.g. assignment compatibility, return types, operator types)

## Project Structure

```
Typecheck/
  Main.java                  # Entry point, runs all passes in order
  TypeCheckException.java    # Exception thrown on type errors
  Pass/
    Pass.java                # Base visitor that traverses the AST
    ScopePass.java           # Base class for passes that need scope context
    CreateScopePass.java     # Pass 1: builds the scope tree
    TypeScopePass.java       # Pass 2: registers types into scope
    FunAndVarScopePass.java  # Pass 3: registers functions and variables
    JudgementsPass.java      # Pass 4: enforces type rules
  SymbolTable/
    Scope.java               # Scope node with parent chain and symbol lookup
    VarSymbol.java           # Symbol for variables
    FunSymbol.java           # Symbol for functions
    TypeSymbol.java          # Symbol for types
  Types/
    Type.java                # Base type class
    INT.java, STRING.java, VOID.java, ARRAY.java, POINTER.java, ...
Absyn/
  # AST node classes (FunDecl, IfStmt, VarDecl, etc.)
```

## Running

```bash
make
./run.sh <source-file>
```

A successful type check prints `Type Check Passed!`. A type error prints a `TypeCheckError` message.

## Person 1 Contributions — Scope Infrastructure

Implemented `ScopePass.java` and `CreateScopePass.java`, which form the foundation for all scope-aware passes.

- `CreateScopePass` walks the AST and creates a `Scope` object for each block, linking child scopes to their parent to enable lexical variable lookup.
- `ScopePass` is the base class extended by later passes. It navigates the scope tree by switching `currentscope` before visiting a block's children and restoring it after, ensuring every node is checked in the correct scope context.
