import java.util.*;

public class run {
    private static int n, a, t;
    private static String input, output;
    private static Set<Integer> acceptStates;
    private static Set<Integer> possibleStates;
    private static Vector<Map> v;
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        input = sc.nextLine();
        n = sc.nextInt();
        a = sc.nextInt();
        t = sc.nextInt();
        acceptStates = new HashSet<>();
        for (int i = 0; i < a; i++) {
            acceptStates.add(sc.nextInt());
        }

        v = new Vector<>(n);

        for (int j = 0; j < n; j++) {
            int temp = sc.nextInt();
            Map<String, Set<Integer>> map = new HashMap<>();

            if (temp == 0) {
                v.add(j, map);
                continue;
            }

            for (int x = 0; x < temp; x++) {
                String ch = sc.next();
                if (map.containsKey(ch)) {
                    map.get(ch).add(sc.nextInt());
                } else {
                    Set<Integer> set = new HashSet<>();
                    set.add(sc.nextInt());
                    map.put(ch, set);
                }
            }
            v.add(j, map);
        }

        check();
        System.out.println(output);
    }

    public static void check(){
        output = "";
        possibleStates = new HashSet<>();
        possibleStates.add(0);

        for (int i = 0; i < input.length(); i++){
            if(helper("" + input.charAt(i))){
                output += 'Y';
            }else{
                output += 'N';
            }
        }
        output += '\n';
    }

    private static boolean helper(String c){
        if(possibleStates.isEmpty()) return false;

        Set<Integer> set = new HashSet<>();

        for (int x: possibleStates) {
            Map<String, Set<Integer>> temp = v.get(x);
            if(temp.containsKey(c)){
                Set<Integer> cur = temp.get(c);
                for (int state: cur) {
                    set.add(state);
                }
            }
        }

        possibleStates = set;

        for (int y: possibleStates) {
            if(acceptStates.contains(y)){
                return true;
            }
        }
        return false;
    }
}