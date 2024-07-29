import java.util.*;

public class build {
    private static String input;
    private static class NFA{
        private Set<Integer> acceptStates;
        private Vector<Map> states;
        private int numberOfStates;
        public NFA(){
            acceptStates = new HashSet<>();
            states = new Vector<>();
        }
    }
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        input = sc.next();

        input = changeInput(input);

        String postfix = convertToPostfix(input);

        NFA result = buildNFA(postfix);
        result = checkForEmptyExp(result);
        printNFA(result);
    }

    private  static String changeInput(String input){
        String changedInput = "";
        int count = 0;

        if(!"()*|".contains(""+input.charAt(0))) count++;

        changedInput += input.charAt(0);

        for(int i = 1; i < input.length(); i++){
            char ch = input.charAt(i);
            char prev = input.charAt(i - 1);
            if(!"()*|".contains(""+ch)) count++;
            if(count >= 1 && !"(|".contains("" + prev) && !")*|".contains("" + ch)){
                changedInput += '.';
            }else if(prev == '(' && ch == ')'){
                changedInput += '?';
            }
            changedInput += ch;
        }

        return changedInput;
    }

    private static String convertToPostfix(String expression){
        String postfix = "";
        Stack<Character> toPostfix = new Stack<Character>();
        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);
            if (c == '(') {
                toPostfix.add(c);
            }else if(c == '*') {
                if (!toPostfix.empty() && toPostfix.peek() == '*') {
                    postfix += c;
                }else{
                    toPostfix.add(c);
                }
            }else if(c == '.'){
                while(!toPostfix.isEmpty() && ".*".contains("" + toPostfix.peek())){
                    postfix += toPostfix.pop();
                }
                toPostfix.add(c);
            }else if(c == '|') {
                while (!toPostfix.empty() && "*|.".contains("" + toPostfix.peek())){
                    postfix += toPostfix.pop();
                }
                toPostfix.add(c);
            }else if(c == ')') {
                while (toPostfix.peek() != '(') {
                    postfix += toPostfix.pop();
                }
                toPostfix.pop();
            }else{
                postfix += c;
            }
        }
        while(!toPostfix.empty()){
            postfix += toPostfix.pop();
        }
        return postfix;
    }

    private static boolean isExpression(Character c){
        return "*|.".contains(""+c);
    }

    private static NFA buildNFA(String postfix){
        Stack<NFA> stack = new Stack<>();
        for(int i = 0; i < postfix.length(); i++){
            char symbol = postfix.charAt(i);
            if(!isExpression(symbol)){
                NFA n = new NFA();
                n.numberOfStates = 2;
                n.acceptStates.add(1);
                Map<Character, Set<Integer>> map = new HashMap<>();
                Set<Integer> set = new HashSet<>();
                set.add(1);
                map.put(symbol, set);
                n.states.add(0,map);
                n.states.add(1,new HashMap<>());
                stack.add(n);
            }else if(symbol == '.'){
                NFA first = stack.pop();
                NFA second = stack.pop();
                NFA newNFA = concatenation(second, first);
                stack.add(newNFA);
            }else if(symbol == '|'){
                NFA first = stack.pop();
                NFA second = stack.pop();
                NFA newNFA = union(second, first);
                stack.add(newNFA);
            }else if(symbol == '*'){
                NFA first = stack.pop();
                NFA newNFA = star(first);
                stack.add(newNFA);
            }
        }

        return stack.pop();
    }


    private static NFA concatenation(NFA first, NFA second) {
        int diff = first.numberOfStates - 1;

        for(int a: first.acceptStates){
            Map<Character, Set<Integer>> m1 = first.states.get(a);
            Map<Character, Set<Integer>> m2 = second.states.get(0);

            m1 = helper(m1, m2, diff);
            first.states.set(a, m1);
        }

        first.numberOfStates = diff + second.numberOfStates;

        if(second.acceptStates.contains(0)){
            first.acceptStates.addAll(setIncrement(second.acceptStates, diff));
        }else{
            first.acceptStates = setIncrement(second.acceptStates, diff);
        }
        return updateOtherStates(first, second, diff);
    }

    private static NFA union(NFA first, NFA second) {
        int diff = first.numberOfStates - 1;
        first.acceptStates.addAll(setIncrement(second.acceptStates, diff));
        first.numberOfStates = second.numberOfStates + diff;

        Map<Character, Set<Integer>> m1 = first.states.get(0);
        Map<Character, Set<Integer>> m2 = second.states.get(0);

        m1 = helper(m1, m2, diff);
        first.states.set(0, m1);

        if(first.acceptStates.contains(0) || second.acceptStates.contains(0)){
            first.acceptStates.add(0);
        }

        return updateOtherStates(first, second, diff);
    }

    private static NFA star(NFA first) {
        for(int a: first.acceptStates) {
            Map<Character, Set<Integer>> m1 = first.states.get(0);
            Map<Character, Set<Integer>> m2 = first.states.get(a);

            m2 = updateMap(m1, m2);
            first.states.set(a, m2);
        }

        first.acceptStates.add(0);
        return first;
    }

    private static Set<Integer> setIncrement(Set<Integer> s, int x){
        Set<Integer> res = new HashSet<>();
        for (int i: s) {
            i += x;
            res.add(i);
        }
        return res;
    }

    private static NFA updateOtherStates(NFA first, NFA second, int diff){
        for(int j = 1; j < second.states.size(); j++){
            Map<Character, Set<Integer>> m = second.states.get(j);
            for(Character c: m.keySet()){
                m.replace(c,setIncrement(m.get(c), diff));
            }
            first.states.add(j + diff, m);
        }
        return first;
    }

    private static Map<Character, Set<Integer>> helper(Map<Character, Set<Integer>> m1,
                                                       Map<Character, Set<Integer>> m2, int diff){
        for(Character c: m2.keySet()){
            Set<Integer> set = setIncrement(m2.get(c), diff);
            if(!m1.containsKey(c)){
                m1.put(c, set);
            }else{
                m1.get(c).addAll(set);
            }
        }
        return  m1;
    }
    private static void printNFA(NFA result) {
        int n = result.numberOfStates;
        int a = result.acceptStates.size();
        int t = 0;

        Vector<Integer> numberOfMoves = new Vector<>(n);

        for(int i = 0; i < n; i++){
            int temp = 0;
            Map<Character, Set<Integer>> m = result.states.get(i);
            if(m.isEmpty()){
                temp = 0;
            }else {
                for (Character c : m.keySet()) {
                    temp += m.get(c).size();
                }
            }
            t += temp;
            numberOfMoves.add(i, temp);
        }

        System.out.println(n + " " + a + " " + t);
        printAcceptStates(result);
        printMoves(result, numberOfMoves);
    }

    private static void printAcceptStates(NFA result){
        String output = "";
        for(int acc: result.acceptStates){
            output += acc + " ";
        }
        System.out.println(output.trim());
    }
    private static void printMoves(NFA result, Vector<Integer> numberOfMoves){
        String output = "";
        for(int x = 0; x < result.numberOfStates; x++){
            output = "" + numberOfMoves.get(x) + " ";
            Map<Character, Set<Integer>> map = result.states.get(x);

            for(Character ch: map.keySet()){
                for(int y: map.get(ch)){
                    output = output + ch + " " + y + " ";
                }
            }

            System.out.println(output);
        }
    }

    private static Map<Character, Set<Integer>> updateMap(Map<Character, Set<Integer>> m1,
                                                          Map<Character, Set<Integer>> m2){
        for (Character c : m1.keySet()) {
            if (!m2.containsKey(c)) {
                m2.put(c, m1.get(c));
            }else{
                m2.get(c).addAll(m1.get(c));
            }
        }
        return m2;
    }

    private static NFA checkForEmptyExp(NFA result) {
        for(int i = 0; i < result.numberOfStates; i++){
            Map<Character, Set<Integer>> map = result.states.get(i);
            if(!map.containsKey('?')) continue;

            for(int x: map.get('?')){
                if(result.acceptStates.contains(x) && !result.acceptStates.contains(i)){
                    result.acceptStates.add(i);
                }

                Map<Character, Set<Integer>> next = result.states.get(x);
                map = updateMap(next, map);
                map.remove('?');
                result.states.set(i, map);
            }
        }
        return result;
    }
}