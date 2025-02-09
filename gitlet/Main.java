package gitlet;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author Helia
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        // TODO: what if args is empty?
        if (args.length == 0) { // ❓check if the args is empty: check length - but why not null?
            System.out.println("Usage: gitlet [path]");
        }
        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                // TODO: handle the `init` command
                Repository.init();
                break;
            case "add":
                // TODO: handle the `add [filename]` command
                if (args.length < 2) {
                    System.out.println("plz enter file name");
                }
                Repository.add(args[1]);
                break;
            // TODO: FILL THE REST IN
        }
    }
}
