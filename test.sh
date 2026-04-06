#!/bin/bash

# Make sure the project is compiled first
./run.sh > /dev/null 2>&1

echo "--- STARTING GEAUX TYPECHECKER TEST SUITE ---"
echo "---------------------------------------------"

for testfile in tests/*.g; do
    echo -n "Running $testfile... "

    # Run the compiler and capture the output/exit code
    # Adjust this line if your run.sh doesn't return non-zero on Error
    output=$(./run.sh "$testfile" 2>&1)

    if [[ $output == *"TypeCheckError"* ]]; then
        if [[ $testfile == tests/f* ]]; then
            echo -e "\e[32m[CORRECT]\e[0m Caught expected error."
        else
            echo -e "\e[31m[FAILED]\e[0m Threw error on valid code."
            echo "$output" | grep "TypeCheckError"
        fi
    else
        if [[ $testfile == tests/p* ]]; then
            echo -e "\e[32m[CORRECT]\e[0m Passed valid code."
        else
            echo -e "\e[31m[FAILED]\e[0m Failed to catch error in invalid code."
        fi
    fi
done

echo "---------------------------------------------"
echo "Test Suite Complete."