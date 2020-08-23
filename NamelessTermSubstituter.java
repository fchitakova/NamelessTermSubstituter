package com.company;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


public class NamelessTermSubstituter {
    
    private static String M;
    private static String variable;
    private static String N;
    private static BufferedReader reader;
    private static int lambdasCountInM = 0;
    
    public static void main(String[] args) throws IOException {
	reader = new BufferedReader(new InputStreamReader(System.in));
	
	while(true) {
	    
	    System.out.println("Enter M:");
	    M = reader.readLine();
	    
	    System.out.println("\nEnter variable to substitute:");
	    variable = reader.readLine();
	    
	    System.out.println("\nEnter N:");
	    N = reader.readLine();
	    
	    
	    System.out.println(substituteAndClear(M.trim(), variable.trim(), N.trim()));
	}
    }
    
    public static String substituteAndClear(String M, String variableToSubstitute, String N) {
	String result = substitute(M, variableToSubstitute, N);
	clear();
	return result;
    }
    
    private static boolean mustShift(String M, String N, Integer variableToSubstituteInM) {
	List<Integer> boundVariablesOfM = getBoundVariablesOf(M);
	List<Integer> freeVariablesOfN = getFreeVariablesOf(N);
	
	if((lambdasCountInM + getAbstractionsCount(N)) - (variableToSubstituteInM + 1) >= 0) {
	    return true;
	}
	
	for(Integer i : boundVariablesOfM) {
	    if(freeVariablesOfN.contains(i)) {
		return true;
	    }
	}
	return false;
    }
    
    private static boolean isNumber(char c) {
	return c >= '0' && c<='9';
    }
    
    private static String getVariable(String term, int variableStartIndex) {
	StringBuilder variable = new StringBuilder();
	int i = variableStartIndex;
	while(i<term.length() && isNumber(term.charAt(i))) {
	    variable.append(term.charAt(i));
	    ++i;
	}
	return variable.toString();
    }
    
    private static void clear() {
	lambdasCountInM = 0;
    }
    
    private static String substitute(String M, String variableToSubstitute, String N) {
	if(M.length()!=0) {
	    if(M.startsWith("(lm")) {
		++lambdasCountInM;
		String wholeSubterm = getWholeSubterm(M, 0);
		String subterm = wholeSubterm.substring(3, wholeSubterm.length() - 1);
		
		
		int variableToSubstituteAsInt = Integer.parseInt(variableToSubstitute);
		if(variableToSubstituteAsInt - lambdasCountInM<0 && getFreeVariablesOf(subterm)
									    .contains(variableToSubstituteAsInt)) {
		    ++variableToSubstituteAsInt;
		}
		String newVariableToSubstitute = String.valueOf(variableToSubstituteAsInt);
		
		
		String subtermAfterSubstitution;
		if(mustShift(wholeSubterm, N, variableToSubstituteAsInt)) {
		    return "(lm" + substitute(subterm, newVariableToSubstitute, shiftFVN(N)) + ')' + substitute(M.substring(wholeSubterm
																    .length()), variableToSubstitute, N);
		} else {
		    return "(lm" + substitute(subterm, newVariableToSubstitute, N) + ')' + substitute(M.substring(wholeSubterm
															  .length()), variableToSubstitute, N);
		}
		
	    }
	    
	    if(M.startsWith("lm")) {
		++lambdasCountInM;
		String subterm = getAbstractionSubterm(M, 0);
		
		int variableToSubstituteAsInt = Integer.parseInt(variableToSubstitute);
		if(variableToSubstituteAsInt - lambdasCountInM<0 && getFreeVariablesOf(subterm)
									    .contains(variableToSubstituteAsInt)) {
		    ++variableToSubstituteAsInt;
		}
		String newVariableToSubstitute = String.valueOf(variableToSubstituteAsInt);
		
		if(mustShift(getWholeSubterm(M, 0), N, variableToSubstituteAsInt)) {
		    return "lm" + substitute(subterm, newVariableToSubstitute, shiftFVN(N));
		} else {
		    return "lm" + substitute(subterm, newVariableToSubstitute, N);
		}
	    }
	    
	    if(M.charAt(0)=='(' || M.charAt(0)==')' || M.charAt(0)==' ') {
		return M.charAt(0) + substitute(M.substring(1), variableToSubstitute, N);
	    }
	    if(startsWithVariable(M)) {
		String variableSubstitutionResult;
		String variable = getVariable(M, 0);
		
		if(areVariablesEqual(variable, variableToSubstitute)) {
		    if(!isNumericVariable(N)) {
			variableSubstitutionResult = '(' + N + ')';
		    } else {
			variableSubstitutionResult = N;
		    }
		} else {
		    variableSubstitutionResult = variable;
		}
		return variableSubstitutionResult + substitute(M.substring(variable.length()), variableToSubstitute, N);
	    }
	}
	return "";
    }
    
    
    private static String getWholeSubterm(String term, int numberOfBrackets) {
	StringBuilder result = new StringBuilder();
	if(term.startsWith("lm")) {
	    result.append("lm" + getAbstractionSubterm(term, numberOfBrackets));
	} else {
	    result.append("(lm" + getAbstractionSubterm(term, 1));
	}
	return result.toString();
    }
    
    private static int getAbstractionsCount(String term) {
	int count = 0;
	
	for(int i = 0; i<term.length(); i++) {
	    if(term.substring(i).startsWith("lm")) {
		++count;
		i += 2;
	    }
	}
	return count;
    }
    
    private static String getAbstractionSubterm(String term, int numberOfBrackets) {
	StringBuilder subterm = new StringBuilder();
	
	int i = 0;
	if(term.startsWith("(lm")) {
	    i += 3;
	} else {
	    i += 2;
	}
	while(i<term.length()) {
	    if(term.charAt(i)=='(') {
		++numberOfBrackets;
		subterm.append(term.charAt(i));
		++i;
		continue;
	    }
	    if(term.charAt(i)==')' && numberOfBrackets>0) {
		--numberOfBrackets;
		subterm.append(term.charAt(i));
		if(numberOfBrackets==0) {
		    break;
		}
		++i;
		continue;
	    }
	    if(term.substring(i).startsWith("lm") && numberOfBrackets==0) {
		return subterm.toString();
	    }
	    subterm.append(term.charAt(i));
	    ++i;
	}
	
	return subterm.toString();
    }
    
    private static boolean startsWithVariable(String term) {
	Character c = term.charAt(0);
	return isNumber(c);
    }
    
    private static boolean isNumericVariable(String str) {
	try {
	    Integer.parseInt(str);
	} catch(NumberFormatException e) {
	    return false;
	}
	return true;
    }
    
    private static String shiftFVN(String N) {
	StringBuilder resultN = new StringBuilder();
	int lambdasCountInN = 0;
	
	for(int i = 0; i<N.length(); i++) {
	    if(N.substring(i).startsWith("lm")) {
		resultN.append("lm");
		i += 1;
		++lambdasCountInN;
	    } else {
		if(N.charAt(i)=='(' || N.charAt(i)==')' || N.charAt(i)==' ') {
		    resultN.append(N.charAt(i));
		} else {
		    String variable = getVariable(N, i);
		    int numericVariable = Integer.parseInt(variable);
		    
		    if(getFreeVariablesOf(N).contains(numericVariable) && (numericVariable - 1) - (lambdasCountInN + lambdasCountInM)<=0) {
			numericVariable += 1;
		    }
		    resultN.append(numericVariable);
		}
	    }
	}
	return resultN.toString();
    }
    
    private static boolean areVariablesEqual(String firstVariable, String secondVariable) {
	return Integer.parseInt(firstVariable)==Integer.parseInt(secondVariable);
    }
    
    private static List<Integer> getBoundVariablesOf(String term) {
	List<Integer> boundVariables = new ArrayList<>();
	int lambdasCount = 0;
	
	for(int i = 0; i<term.length(); i++) {
	    if(term.substring(i).startsWith("lm")) {
		++lambdasCount;
		i += 1;
	    }
	    if(term.substring(i).startsWith("(lm")) {
		++lambdasCount;
		i += 2;
	    }
	    
	    if(isNumber(term.charAt(i))) {
		Integer variable = Integer.parseInt(getVariable(term, i));
		i += getVariable(term, i).length();
		
		if(lambdasCount>0 && (lambdasCount - (variable + 1) >= 0)) {
		    boundVariables.add(variable);
		}
	    }
	}
	return boundVariables;
    }
    
    private static List<Integer> getFreeVariablesOf(String term) {
	List<Integer> freeVariables = new ArrayList<>();
	int lambdasCount = 0;
	
	for(int i = 0; i<term.length(); i++) {
	    if(term.substring(i).startsWith("lm")) {
		++lambdasCount;
		i += 1;
	    }
	    if(term.substring(i).startsWith("(lm")) {
		++lambdasCount;
		i += 2;
	    }
	    
	    if(isNumber(term.charAt(i))) {
		Integer variable = Integer.parseInt(getVariable(term, i));
		i += getVariable(term, i).length();
		
		if(lambdasCount - (variable + 1)<0) {
		    freeVariables.add(variable);
		}
	    }
	}
	return freeVariables;
    }
    
}








