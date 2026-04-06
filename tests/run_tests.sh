#!/bin/bash
PASS=0
FAIL=0
run_test() {
    local file=$1
    local expect_pass=$2
    local name=$(basename $file)
    output=$(./run.sh "$file" 2>&1)
    has_error=0
    echo "$output" | grep -q "TypeCheckError" && has_error=1
    if [ $expect_pass -eq 1 ] && [ $has_error -eq 0 ]; then
        echo -e "  \e[32mPASS\e[0m  $name"
        ((PASS++))
    elif [ $expect_pass -eq 0 ] && [ $has_error -eq 1 ]; then
        echo -e "  \e[32mPASS\e[0m  $name"
        ((PASS++))
    elif [ $expect_pass -eq 1 ] && [ $has_error -eq 1 ]; then
        echo -e "  \e[31mFAIL\e[0m  $name -- expected clean but got: $(echo "$output" | grep TypeCheckError)"
        ((FAIL++))
    else
        echo -e "  \e[31mFAIL\e[0m  $name -- expected error but passed cleanly"
        ((FAIL++))
    fi
}
echo "============================================"
echo " Rule 1  - Numbers vs strings"
echo "============================================"
run_test tests2/f_rule1a_int_from_string.g 0
run_test tests2/f_rule1b_string_from_int.g 0
echo ""
echo "============================================"
echo " Rule 2  - Pointers count as numbers"
echo "============================================"
run_test tests2/p_rule2a_pointer_assign_num.g 1
run_test tests2/p_rule2b_int_from_pointer.g 1
run_test tests2/p_rule2c_pointer_math.g 1
echo ""
echo "============================================"
echo " Rule 3  - Fixed array init length"
echo "============================================"
run_test tests2/p_rule3a_fixed_array_correct.g 1
run_test tests2/f_rule3b_fixed_array_wrong_length.g 0
echo ""
echo "============================================"
echo " Rule 4  - Struct init list"
echo "============================================"
run_test tests2/p_rule4a_struct_valid.g 1
run_test tests2/f_rule4b_struct_wrong_members.g 0
echo ""
echo "============================================"
echo " Rule 5  - Union init (one member only)"
echo "============================================"
run_test tests2/p_rule5a_union_member1.g 1
run_test tests2/p_rule5b_union_member2.g 1
run_test tests2/f_rule5c_union_full_list.g 0
echo ""
echo "============================================"
echo " Rule 6  - Open array consistency"
echo "============================================"
run_test tests2/p_rule6a_open_array_valid.g 1
run_test tests2/f_rule6b_open_array_jagged.g 0
echo ""
echo "============================================"
echo " Rule 8  - Math requires numbers"
echo "============================================"
run_test tests2/p_rule8a_math_ints.g 1
run_test tests2/f_rule8b_math_strings.g 0
run_test tests2/f_rule8c_math_string_result.g 0
echo ""
echo "============================================"
echo " Rule 9  - Function call types and arity"
echo "============================================"
run_test tests2/p_rule9a_fun_call_valid.g 1
run_test tests2/f_rule9b_fun_call_wrong_type.g 0
run_test tests2/f_rule9c_fun_call_wrong_count.g 0
echo ""
echo "============================================"
echo " Rule 10 - Return type matching"
echo "============================================"
run_test tests2/p_rule10a_return_valid.g 1
run_test tests2/f_rule10b_return_wrong_type.g 0
run_test tests2/p_rule10c_nested_fun_valid.g 1
run_test tests2/f_rule10d_nested_fun_wrong_return.g 0
run_test tests2/p_rule10e_void_no_return.g 1
run_test tests2/f_rule10f_void_returns_value.g 0
echo ""
echo "============================================"
echo " Rule 11 - Var/fun name collision"
echo "============================================"
run_test tests2/f_rule11a_var_fun_clash.g 0
echo ""
echo "============================================"
echo " Rule 12 - Type must exist in scope"
echo "============================================"
run_test tests2/f_rule12a_undefined_type.g 0
run_test tests2/p_rule12b_defined_type.g 1
run_test tests2/p_rule12c_typedef.g 1
echo ""
echo "============================================"
echo " Rule 13 - If/while condition is number"
echo "============================================"
run_test tests2/p_rule13a_if_int_cond.g 1
run_test tests2/f_rule13b_if_string_cond.g 0
run_test tests2/p_rule13c_while_int_cond.g 1
run_test tests2/f_rule13d_while_string_cond.g 0
echo ""
echo "============================================"
echo " Rule 14 - Unary * and &"
echo "============================================"
run_test tests2/p_rule14a_deref_pointer.g 1
run_test tests2/f_rule14b_deref_nonpointer.g 0
run_test tests2/f_rule14c_deref_int.g 0
run_test tests2/p_rule14d_addr_valid.g 1
run_test tests2/f_rule14e_addr_wrong_target_type.g 0
echo ""
echo "============================================"
echo " Results: $PASS passed, $FAIL failed out of $((PASS+FAIL)) tests"
echo "============================================"
