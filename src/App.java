import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * App, class,
 * This represents the main class from Application Manager,
 * This represents an app that handles files and folders in general
 *
 * @author biologysiwell (23/04/2018 20:46)
 * @since  0.1
 * @version 0.1
 */
public final class App {

    /**
     * Date Format,
     * This represents a variable that format dates
     *
     * @since  0.1
     */
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    /**
     * Keys,
     * This map represents the keys and data values from keys
     *
     * @since  0.1
     */
    private static final Map<String, String> KEYS = new LinkedHashMap<>();

    /**
     * Temp Folder,
     * This represents the variable that contains the temporary folder
     *
     * @since 0.1
     */
    private static final File BATCH_FOLDER = new File("batchs");


    /**
     * Scanner,
     * This represents a variable that scan input strings from console input
     *
     * @since  0.1
     */
    private static Scanner scanner;

    /**
     * Robot,
     * This variable represents a robot that is used to insert editable texts,
     * to make the user can edit lines that contains in files
     *
     * @since 0.1
     */
    private static Robot robot;

    /**
     * Main,
     * This represents the main method that is called by JVM to starts Application Manager
     *
     * @since  0.1
     */
    public static void main(String[] args) {
        printf("\n");
        printf("  Application Manager v0.1\n");
        printf("  Type \"help\" to see commands\n");
        printf("\n");

        // @Note Initialize Application
        initApp();

        // @Note Load Application Manager Database
        loadApplicationManagerDb();

        // @Note This method parse the current command input
        parseCommand();
    }

    /**
     * Parse Command,
     * This method parse the command input from console input
     *
     * @since  0.1
     */
    public static void parseCommand() {
        final String[] args = getCommand().split(" "); /* split arguments by " " */
        final String command = args[0];

        // @Note Help command operation
        if (command.equalsIgnoreCase("help")) {
            // @Note This method print the all help commands from Application Manager
            printHelpCommands();
            parseCommand(); // Recursive
            return;
        }
        // @Note Exit command operation
        else if (command.equalsIgnoreCase("exit")) {
            printf("Good bye! \\o/\n");

            // @Note Not use System.exit(-1), this method is more secure to use
            exit();
            return;
        }
        // @Note Register command operation
        else if (command.equalsIgnoreCase("register") || command.equalsIgnoreCase("reg")) {
            // @Note Check if command register has the necessary arguments
            if (args.length < 3) {
                usage("register", "<key> <path>");
                parseCommand(); // Recursive
                return;
            }

            final String key = args[1];

            // @Note The path represents the append from the arguments from 2 index
            final StringBuilder path = new StringBuilder();
            for (int i = 2; i < args.length; i++) {
                path.append(args[i]).append(' ');
            }

            // @Note Check if this key exists on KEYS storage
            if (KEYS.containsKey(key)) {
                printf("Key \"%\" can not be created, because exists.\n", key);
                parseCommand(); // Recursive
                return;
            }

            // @Note Check if the path length is bigger than 0
            if (path.length() == 0) {
                printf("Key \"%\" can not be created, because path must be length bigger than 0\n");
                parseCommand();
                return;
            }

            // @Note This methdo register the key and value
            registerKey(key, path.toString());

            printf("Key \"%\" has been registered.\n", key);
            parseCommand();
            return;
        }
        // @Note Delete command operation
        else if (command.equalsIgnoreCase("delete") || command.equalsIgnoreCase("del")) {
            // @Note Check if delete command has the necessary arguments
            if (args.length < 2) {
                usage("delete", "<key>");
                parseCommand(); // Recursive
                return;
            }

            final String key = args[1];

            // @Note Check if this key contains on KEYS values
            if (!KEYS.containsKey(key)) {
                printf("Key \"%\" can not be deleted, because not exists.\n", key);
                parseCommand(); // Recursive
                return;
            }

            // @Note Not is recommended to use KEYS.remove(key) because this method is not secure, is recommended to use
            // deleteKey(key) method that deletes the batch file
            deleteKey(key);

            printf("Key \"%\" has been deleted.\n", key);
            parseCommand(); // Recursive
            return;
        }
        // @Note Edit command operation
        else if (command.equalsIgnoreCase("edit")) {
            // @Note Check if the edit command has the necessary arguments
            if (args.length < 3) {
                usage("edit", "<key> <new path>");
                parseCommand(); // Recursive
                return;
            }

            final String key = args[1];
            final String newPath = args[2];

            // @Note Check if this key exists on KEYS storage
            if (!KEYS.containsKey(key)) {
                printf("Key \"%\" can not be changed, because not exists.\n", key);
                parseCommand(); // Recursive
                return;
            }

            // @Note Check if the path length is bigger than 0
            if (newPath.length() == 0) {
                printf("Key \"%\" can not be created, because new path must be length bigger than 0\n");
                parseCommand();
                return;
            }

            // @Note Delete the last batch file that contains key, and register the key with the new path that creates
            // the new batch file with the new path
            deleteKey(key);
            registerKey(key, newPath);

            printf("Key \"%\" has been changed.\n", key);

            parseCommand(); // Recursive
            return;
        }
        // @Note List command operation
        else if (command.equalsIgnoreCase("list")) {
            int index = 1;
            printf("Keys Size: %.\n", KEYS.size());

            // @Note If keys size is equals 0, send another message
            if (KEYS.size() == 0) {
                printf("Nothing registered key.\n");
            } else {
                for (final Map.Entry<String, String> entry : KEYS.entrySet()) {
                    printf(" #% - %: %\n", index++, entry.getKey(), entry.getValue());
                }
            }

            parseCommand(); // Recursive
            return;
        }
        // @Note Run command operation
        else if (command.equalsIgnoreCase("run")) {
            // @Note Check if run command has necessary arguments
            if (args.length < 2) {
                usage("run", "<key>");
                parseCommand(); // Recursive
                return;
            }

            final String key = args[1];

            // @Note Check if this key exists on KEYS storage
            if (!KEYS.containsKey(key)) {
                printf("Key \"%\" can not be run, because not exists.\n", key);
                parseCommand(); // Recursive
                return;
            }

            // @Note This method run the key
            runKey(key);

            printf("Running %...\n", key);
            parseCommand(); // Recursive
            return;
        }
        // @Note Destroy command operation
        else if (command.equalsIgnoreCase("destroy")) {
            printf("Continue the process?\n");
            printf("Y/N? > ");

            // @Note This variable represents the input from destroy confirmation string
            final String destroyConfirmationString = scanner.nextLine();
            final boolean destroyConfirmation;

            if (destroyConfirmationString.equalsIgnoreCase("Y") || destroyConfirmationString.equalsIgnoreCase("YES")) {
                destroyConfirmation = true;
            } else if (destroyConfirmationString.equalsIgnoreCase("N") || destroyConfirmationString.equalsIgnoreCase("NO")) {
                destroyConfirmation = false;
            } else {
                printf("Destroy confirmation is not found. Destroy operation cancelled!\n");
                parseCommand(); // Recursive
                return;
            }

            // @Note Destroy method makes the destruction from the Application Manager Database,
            // and the returned value from "destroy" method represents if the destroy from application is failed or
            // success
            boolean destroyApp = destroy();

            if (destroyApp) {
                printf("Application Manager has been destroyed.\n");
                printf("Now the Application will be shutdown on 3 seconds.\n");
                try {
                    Thread.sleep(3000L);
                } catch (InterruptedException e) {
                    error("An error occured when shutdown Application after application destroy. (Report to an administrator)\n");
                    e.printStackTrace();
                }
                exit();
            } else {
                error("Application Destruction has been failed. (Report to an administrator)\n");
                printf("Now the Application will be shutdown on 3 seconds.\n");
                try {
                    Thread.sleep(3000L);
                } catch (InterruptedException e) {
                    error("An error occured when shutdown Application after application destroy. (Report to an administrator)\n");
                    e.printStackTrace();
                }
                exit();
            }

            return;
        }
        // @Note Mkfile command operation
        else if (command.equalsIgnoreCase("mkfile")) {
            // @Note Check if the command has the necessary arguments to continue
            if (args.length < 2) {
                usage("mkfile", "<file path> [ -rc ]");

                parseCommand(); // Recursive
                return;
            }

            final String path = args[1];
            final File file = new File(path);
            final boolean recreates = hasFlag(args, "-rc");

            // @Note Check if the file exists
            if (file.exists()) {
                if (recreates) {
                    if (file.isDirectory()) {
                        error("File in path \"%\" is a directory.\n", path);
                        parseCommand(); // Recursive
                        return;
                    }

                    // @Note This method delete the file that exists and command that contains flag "-rc",
                    // this operation make the delete from file to creates the file on the final process
                    file.delete();
                } else {
                    printf("Can not continue this process, because file in path \"%\" exists. (To bypass this process use \"-rc\" flag.\n", path);
                    parseCommand(); // Recursive
                    return;
                }
                // @Note Not put "return" on this line
            }

            try {
                file.createNewFile();
            } catch (IOException e) {
                error("An error occured when make file in path \"%\".\n", path);
                e.printStackTrace();
            }

            printf("File in path \"%\" has been created.\n", path);
            parseCommand(); // Recursive
            return;
        }
        // @Note Mkdir command operation
        else if (command.equalsIgnoreCase("mkdir")) {
            // @Note Check if the command has the necessary arguments to continue.
            if (args.length < 2) {
                usage("mkdir", "<file path> [ -rc ]");

                parseCommand(); // Recursive
                return;
            }

            final String path = args[1];
            final File file = new File(path);
            final boolean recreates = hasFlag(args, "-rc");

            if (file.exists()) {
                if (recreates) {
                    // @Note This method delete the file that exists and command that contains flag "-rc",
                    // this operation make the delete from file to creates the file on the final process
                    delete(file);
                } else {
                    printf("Can not continue this process, because directory in path \"%\" exists. (To bypass this process use \"-rc\" flag.\n", path);
                    parseCommand(); // Recursive
                    return;
                }
                // @Note Not put "return" in this line
            }

            final boolean mkdir = file.mkdir();
            if (!mkdir) {
                error("An error occured in make directory in path \"%\". (Report to an administrator)\n", path);
                parseCommand(); // Recursive
                return;
            }

            printf("Directory in path \"%\" has been created.\n", path);
            parseCommand(); // Recursive
            return;
        }
        // @Note Delete file command operation
        else if (command.equalsIgnoreCase("deletefile") || command.equalsIgnoreCase("delfile")) {
            // @Note Check if the command has the necessary arguments
            if (args.length < 2) {
                usage("deletefile", "<file path>");

                parseCommand(); // Recursive
                return;
            }

            final String path = args[1];
            final File file = new File(path);

            // @Note Check if the file exists
            if (!file.exists()) {
                error("Can not delete this file in path \"%\", because not exists.\n", path);
                parseCommand(); // Recursive
                return;
            }

            // @Note Not is recommended use file.delete(), because when the file is directory the files that contains
            // in the directory is not deleted, then use delete()
            delete(file);

            printf("File in path \"%\" has been deleted.\n", path);
            parseCommand(); // Recursive
            return;
        }
        // @Note Write command operation
        else if (command.equalsIgnoreCase("write")) {
            // @Note Check if the command has the necessary arguments
            if (args.length < 2) {
                usage("write", "<file path> [ -a ]");

                parseCommand(); // Recursive
                return;
            }

            final String path = args[1];
            final File file = new File(path);
            final boolean append = hasFlag(args, "-a");

            printf("Write the % content to file.\n", append ? "append" : "new");
            printf("To cancel this operation type \"exit\".\n");
            printf("> ");

            // @Note Get the content line that is write to file
            final String newContent = scanner.nextLine();

            // @Note Sub-commands about "write" command operation
            // Exit sub-command operation from "write" command operation
            if (newContent.equalsIgnoreCase("exit")) {
                printf("Write operation cancelled!\n");

                parseCommand(); // Recursive
                return;
            }

            // @Note This represents the content that is write to file,
            // The content must be read before from FileWriter class,
            // because if not the file is cleared
            final String content = (append ? read(file) + System.lineSeparator() : "") + newContent;
            try (final FileWriter writer = new FileWriter(file)) {
                writer.write(content);
            } catch (IOException e) {
                error("An error occured when write content in file path \"%\". (Report to an administrator)\n", path);
                e.printStackTrace();
            }

            printf("File in path \"%\" has been content updated.\n", path);

            parseCommand(); // Recursive
            return;
        }
        // @Note Read command operation
        else if (command.equalsIgnoreCase("read")) {
            // @Note Check if teh command has the necessary arguments
            if (args.length < 2) {
                usage("read", "<file path>");

                parseCommand(); // Recursive
                return;
            }

            final String path = args[1];
            final File file = new File(path);
            final boolean informations = hasFlag(args, "-i");

            // @Note Check if the file exists
            if (!file.exists()) {
                printf("Can not be read the file in path \"%\", because not exists.\n", path);

                parseCommand(); // Recursive
                return;
            }

            // @Note Read the content that contains in the file
            final String[] lines = read(file).split("\n");
            printf("File in path \"%\" content: \n", path);

            // @Note Print the all lines that contains in the file
            for (final String line : lines) {
                printf("%\n", line);
            }

            // @Note Check if the command operation has the informations flag "-i"
            if (informations) {
                try {
                    final BasicFileAttributes attributes = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
                    printf("\n");
                    printf("Lines: %.\n", lines.length);
                    printf("File path: %.\n", path);

                    printf("Create date: %.\n", DATE_FORMAT.format(attributes.creationTime().toMillis()));
                    printf("Last access: %.\n", DATE_FORMAT.format(attributes.lastAccessTime().toMillis()));
                    printf("Last modification: %.\n", DATE_FORMAT.format(attributes.lastModifiedTime().toMillis()));
                } catch (Exception e) {
                    error("An error occured when show informations about file read content in \"read\" command operation. (Report to an administrator).\n");
                    e.printStackTrace();
                }
            }

            parseCommand(); // Recursive
            return;
        }
        // @Note Edit File command operation
        else if (command.equalsIgnoreCase("editfile")) {
            // @Note Check if the command has the necessary arguments
            if (args.length < 2) {
                usage("editfile", "<file path>");

                parseCommand(); // Recursive
                return;
            }

            final String path = args[1];
            final File file = new File(path);

            // @Note Check if the file exists
            if (!file.exists()) {
                printf("Can not edit this file in path \"%\", because not exists.\n", path);

                parseCommand(); // Recursive
                return;
            }

            // @Note Check if the file is a directory
            if (file.isDirectory()) {
                printf("Can not edit this file in path \"%\", because is a directory.\n", path);

                parseCommand(); // Recursive
                return;
            }

            // @Note Read the content that contains in the file
            final String content = read(file);

            printf("File in path \"%\" content:\n", path);
            printf("Press \"ENTER\" to show content in file.\n");

            // @Note To fix the bug that occurs in insert editable lines, is used ExecutorService to make async
            // operations
            final ExecutorService service = Executors.newSingleThreadExecutor();
            service.submit(() -> {
                // @Note Make that the current thread sleep by 500 milliseconds to the time to display "printf" and
                // scan next line
                try {
                    Thread.sleep(500L);
                } catch (Exception e) {
                    error("An error occured when edit file in path \"%\" (Report to an administrator).", path);
                    e.printStackTrace();
                }

                // @Note Insert editable line content
                insertEditableLine(content);
            });

            printf("> ");
            final String newContent = scanner.nextLine();

            try (final FileWriter writer = new FileWriter(file)) {
                writer.write(newContent);
            } catch (IOException e) {
                error("An error occured when edit file in path \"%\". (Report to an administrator)\n", path);
                e.printStackTrace();
            }

            printf("File in path \"%\" has been updated.\n", path);

            parseCommand(); // Recursive
            return;
        }
        // @Note Copy command operation
        else if (command.equalsIgnoreCase("copy")) {
            // @Note Check if the command has the necessary arguments
            if (args.length < 2) {
                usage("copy", "<from path> <to path>");

                parseCommand(); // Recursive
                return;
            }

            final String fromPath = args[1];
            final String toPath = args[2];

            final File fromFile = new File(fromPath);
            final File toFile = new File(toPath);

            // @Note Copy source file content to destination file
            copyFile(fromFile, toFile, true);

            parseCommand(); // Recursive
            return;
        }
        // @Note Dir command operation
        else if (command.equalsIgnoreCase("dir")) {
            // @Note Check if the command has the necessary arguments
            if (args.length < 2) {
                usage("dir", "<file path> [file index]");

                parseCommand(); // Recursive
                return;
            }

            final String path = args[1];
            final File file;

            // @Note (args.length == 3) represents that the command has file index, then the file index is parsed
            // by this statement because has two arguments that need to show the directory content that represents the
            // file path and file index
            if (args.length >= 3) {
                final int[] fileIndexes = new int[args.length - 2];
                for (int i = 2, k = 0; i < args.length; i++) {
                    final int index;
                    try {
                        index = Integer.parseInt(args[i]);
                    } catch (Exception e) {
                        error("File index in position \"%\" can not be parsed because must be a number. Actual: \"%\".\n", k, args[i]);

                        parseCommand(); // Recursive
                        return;
                    }

                    fileIndexes[k++] = index;
                }

                File currentFile = null;
                File[] listFiles = new File(path).listFiles();

                for (int index : fileIndexes) {
                    currentFile = listFiles[index];
                    listFiles = currentFile.listFiles();
                }

                // @Note Check if the current file is null
                if  (currentFile == null) {
                    error("File has not found.\n");

                    parseCommand(); // Recursive
                    return;
                }

                // @Note Assign file to current file that found by searching index
                file = currentFile;
            } else {
                // @Note Assign the file by path
                file = new File(path);
            }

            // @Note Check if the file exists
            if (!file.exists()) {
                printf("Can not list files from directory in path \"%\", because not exists.\n", path);

                parseCommand(); // Recursive
                return;
            }

            // @Note Check if the file is a directory
            if (!file.isDirectory()) {
                printf("Can not list files from directory in path \"%\", because not is a directory.\n", path);

                parseCommand(); // Recursive
                return;
            }

            // @Note The model used is from Windows MS-DOS that represents a model
            // that shows the informations from files that contains in directory

            printf("\n");
            printf(" Pasta de %\n", file.getAbsolutePath());
            printf("\n");

            // @Note List the files that contains in the directory
            int i = 0;
            for (final File f : file.listFiles()) {
                printf("#% % % % % %\n", i++, DATE_FORMAT.format(f.lastModified()), "    " + (f.isDirectory() ? "<DIR>" : "     "), (f.isDirectory() ? "" : f.length()), spacing("" + f.length(), 20) + f.getName() + spacing(f.getName(), 20), f.getAbsolutePath());
            }

            parseCommand(); // Recursive
            return;
        }
        // @Note Clear command operation
        else if (command.equalsIgnoreCase("clear")) {
            try {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } catch (Exception e) {
                error("An error occured when clear screen. (Report to an administrator)\n");
                e.printStackTrace();
            }

            parseCommand(); // Recursive
            return;
        }

        printf("Command has not found. Try again!\n");
        parseCommand(); // Recursive
        return;
    }

    // @Note Initialization

    /**
     * Initialize Application,
     * This method initialize the Application Manager
     *
     * @since 0.1
     */
    public static void initApp() {
        try {
            scanner = new Scanner(new InputStreamReader(System.in, "UTF-8"));

            robot = new Robot();
            robot.setAutoDelay(5);
            robot.setAutoWaitForIdle(true);
        } catch (Exception e) {
            error("An error occured when initialize Application Manager. (Report to an administrator)\n");
            e.printStackTrace();
        }
    }

    // @Note Storage

    /**
     * Load Application Manager,
     * This method load the all keys and data values that are storage in database file
     *
     * @since  0.1
     */
    public static void loadApplicationManagerDb() {
        // @Note Create the folder if not exists, and the make from Data Folder
        // need be the first process
        BATCH_FOLDER.mkdirs();

        // @Note This represents the file from database
        final File file = getFileDatabase();

        // @Note Check if the file exists, otherwise returns method
        if (!file.exists()) {
            return;
        }

        try (final BufferedReader reader = new BufferedReader(new FileReader(file))) {
            // @Note This represents the each line that is read,
            // that contains in file
            String line;

            while ((line = reader.readLine()) != null) {
                final String[] keyAndValue = line.split(" ");

                // @Note Put the key and data value into KEYS map
                KEYS.put(keyAndValue[0], keyAndValue[1]);
            }
        } catch (IOException e) {
            error("An error occured when load application manager from database \"%\". (Report to an administrator)\n", file.getAbsolutePath());
            e.printStackTrace();
        }
    }

    /**
     * Save Application Manager,
     * This method save the all keys and data values storaged by Application Manager
     * to file database
     *
     * @since  0.1
     */
    public static void saveApplicationManagerDb() {
        // @Note This represents the file from database
        final File file = getFileDatabase();

        // @Note This methods makes that when save the Application Manager storage makes that the
        // storage can be modified, on the last process from the save from Application Manager storage
        // the file is set writable to false to can avoid modifications out of Application Manager
        file.setWritable(true);

        try (final FileWriter writer = new FileWriter(file)) {
            final StringBuilder sb = new StringBuilder();
            for (final Map.Entry<String, String> entry : KEYS.entrySet()) {
                sb.append(entry.getKey()).append(' ').append(entry.getValue()).append('\n');
            }

            // @Note Check if the string builder length is bigger than 0, because if the
            // string is empty when "deleteCharAt" method is executed can thrown an exception
            if (sb.length() > 0) {
                // @Note This method delete the last character that represents the '\n',
                // that is added on the last for-each loop
                sb.deleteCharAt(sb.length() - 1);
            }

            // @Note Write the StringBuilder content to file
            writer.write(sb.toString());
        } catch (IOException e) {
            error("An error ocurred when save application manager to database \"%\". (Report to an administrator)\n", file.getAbsolutePath());
            e.printStackTrace();
        }

        // @Note This method makes that the file is more secure to modifications into the file,
        // instead of use the Application Manager to change values
        file.setWritable(false);
    }

    /**
     * Get File Database,
     * This method returns the file database that storage the keys and data values
     * that is save by Application Manager
     *
     * @return file database
     */
    public static File getFileDatabase() {
        return new File("keys.data");
    }

    // @Note Print

    /**
     * Print Error Format,
     * This method prints a error formatted message, the varargs of arguments is replaced
     * in message where message has '%'
     *
     * @param message the message that will print to console output
     * @param args    the varargs of arguments
     */
    public static void error(final String message, final Object... args) {
        printf("[ERROR] " + message, args);
    }

    /**
     * Print Format,
     * This method prints a formatted message, the varargs of arguments is replaced
     * in message where message has '%'
     *
     * @param message the message that will print to console output
     * @param args    the varargs of arguments
     * @see  		  {@link #printf(boolean, String, Object...)}
     * @since  0.1
     */
    public static void printf(final String message, final Object... args) {
        printf(true, message, args);
    }

    /**
     * Print Format,
     * This method prints a formatted message, the varargs of arguments is replaced
     * in message where message has '%'
     *
     * @param printWithDate this variable represents that before print message is to show the date
     * @param message 		the message that will print to console output
     * @param args    		the varargs of arguments
     * @see 				{@link #printf(String, Object...)}
     * @since  0.1
     */
    public static void printf(final boolean printWithDate, final String message, final Object... args) {
        if (args.length == 0) {
            System.out.print(printWithDate ? "[" + DATE_FORMAT.format(System.currentTimeMillis()) + "] " + message : message);
        } else {
            final StringBuilder sb = new StringBuilder();
            final char[] charArray = message.toCharArray();

            // @Note 1. Format message
            for (int i = 0, k = 0; i < charArray.length; i++) {
                if (charArray[i] == '%') {
                    sb.append(args[k++]);
                } else {
                    sb.append(charArray[i]);
                }
            }

            // @Note 2. Print formatted message
            System.out.print(printWithDate ? "[" + DATE_FORMAT.format(System.currentTimeMillis()) + "] " + sb.toString() : sb.toString());
        }
    }

    // @Note Utils

    /**
     * Exit,
     * This method close the program, this method is recommended when close the Application Manager,
     * because this method is more secure because this method execute functions before close the program
     *
     * @since  0.1
     */
    public static void exit() {
        // @Note This method save the all application manager content informations to database storage
        saveApplicationManagerDb();
        System.exit(-1);
    }

    /**
     * Get Command,
     * This method prints a ">" that represents the prefix to input command
     *
     * @return command arguments
     * @since  0.1
     */
    public static String getCommand() {
        printf("> ");
        return scanner.nextLine();
    }

    /**
     * Print Help Commands,
     * This method print the all commands from Application Manager
     *
     * @since  0.1
     */
    public static void printHelpCommands() {
        printf("\n");
        printf("Commands: \n");
        printf(" [ COMMAND NAME ], [ COMMAND ALIASES ] [< COMMAND ARGUMENTS >] [[ ARBITRARY FLAGS ]]- [ COMMAND DESCRIPTION ]\n");
        printf(" > copy <from path> <to path>           - Copy a file or directory to a new path.\n");
        printf(" > delete, del <key>                    - Delete a key that is registered.\n");
        printf(" > deletefile, delfile <file path>      - Deletes a file.\n");
        printf(" > destroy                              - Delete Application Manager Database. (!!!!)\n");
        printf(" > dir <file path> [file index]         - List the all files that contains in directory.\n");
        printf(" > edit <key> <new path>                - Edit a value from a key.\n");
        printf(" > editfile <file path>                 - Edit file contnet.\n");
        printf(" > list                                 - List all registered keys.\n");
        printf(" > mkdir <file path> [ - rc ]           - Make a directory.\n");
        printf(" > mkfile <file path> [ -rc ]           - Make a file.\n");
        printf(" > read <file path>                     - Read file content.\n");
        printf(" > register, reg <key> <path>           - Register a path by key.\n");
        printf(" > run <key>                            - Run an application.\n");
        printf(" > write <file path> <content> [ -c ]   - Write a content to a file.\n");
        printf("Flags: \n");
        printf(" [ FLAG NAME ], [ FLAG FUNCTION ] (To display a flag in a command uses prefix \"-\"\n");
        printf(" > a                                    - Append content from file with new content.\n");
        printf(" > i                                    - Show informations about process.\n");
        printf(" > rc                                   - Re-create file if exists in a process.\n");
        printf("\n");
    }

    /**
     * Usage,
     * This method print a formatted message to console output to how use the command
     *
     * @param commandName the command name
     * @param usage       the usage of command
     */
    private static void usage(final String commandName, String usage) {
        printf("Incorrect usage from (%). Use: % %.\n", commandName, commandName, usage);
    }

    /**
     * Register Key,
     * This method register the value from key
     *
     * @param key the key
     * @since 0.1
     */
    private static void registerKey(final String key, final String path) {
        // @Note Check if the key is different from null
        if (key == null) throw new NullPointerException("key");

        // @Note This may be removed, because the register from key is the first process, also from the put the key
        // and value in KEYS map
        // if (!KEYS.containsKey(key)) return;

        final int pathIndex = path.lastIndexOf("\\");
        final String filePath = path.substring(0, pathIndex);
        // @Note (pathIndex + 1) is put because if only put (pathIndex) the first character
        // from filePathName is "\"
        final String filePathName = path.substring(pathIndex + 1, path.length());
        final String fileContentBatch = "@echo off" + System.lineSeparator() + "cd " + filePath + System.lineSeparator() + "start " + filePathName + System.lineSeparator() + "EXIT";

        // @Note StringBuilder is replaced by one String
        // final StringBuilder sb = new StringBuilder();
        // sb.append("@echo off\n").append("start ").append(path);

        try (final FileWriter writer = new FileWriter(getBatchFile(key))) {
            writer.write(fileContentBatch);
        } catch (IOException e) {
            error("An error ocurred when register key \"%\" at path \"%\"", key, path);
            e.printStackTrace();
        }

        // @Note Register the key and path in KEYS
        KEYS.put(key, path);
    }

    /**
     * Delete Key,
     * This method delete the batch file from key
     *
     * @param key the key
     */
    private static void deleteKey(final String key) {
        // @Note Check if the key is null
        if (key == null) throw new NullPointerException("key");

        // @Note Remove the key from KEYS map
        KEYS.remove(key);

        // @Note This method delete the batch file
        getBatchFile(key).delete();
    }

    /**
     * Run Key,
     * This method run the path value from key
     *
     * @param key the key
     * @since 0.1
     */
    private static void runKey(final String key) {
        // @Note Check if the key is different from null
        if (key == null) throw new NullPointerException("key");
        if (!KEYS.containsKey(key)) return;

        // @Note Get the target path that represents the path from the bath file
        final String targetPath = BATCH_FOLDER.getAbsolutePath() + "\\__init_" + key + "__.bat";

        try {
            Runtime.getRuntime().exec("cmd /c start " + targetPath);
        } catch (Exception e) {
            error("An error occured when run key %", key);
        }
    }

    /**
     * Get Batch File,
     * This method get the batch file
     *
     * @param key the key
     * @return batch file
     */
    private static File getBatchFile(final String key) {
        return new File(BATCH_FOLDER, "__init_" + key + "__.bat");
    }

    /**
     * Destroy,
     * This method destroy the all Application Manager Database
     *
     * @since 0.1
     */
    private static boolean destroy() {
        // @Note Clear the all keys and values from KEYS map
        KEYS.clear();

        try {
            // @Note Delete file database
            getFileDatabase().delete();

            // @Note Delete the all files that contains in batch folder and the batch folder
            for (final File file : Objects.requireNonNull(BATCH_FOLDER.listFiles())) {
                file.delete();
            }
            BATCH_FOLDER.delete();
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    /**
     * Delete,
     * This method delete the file or a directory
     *
     * @param file the file
     */
    private static void delete(final File file) {
        if (file.isDirectory()) {
            for (final File f : file.listFiles()) {
                delete(f);
            }
            file.delete();
        } else {
            file.delete();
        }
    }

    /**
     * Has Flag,
     * This method check if in the all string that contains in source array is
     * equals ignore case string flag
     *
     * @param src the source array
     * @param flag the flag
     * @return true if flag contains in string source array, otherwise false
     */
    private static boolean hasFlag(final String[] src, final String flag) {
        for (final String s : src) {
            if (s.equalsIgnoreCase(flag)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Read,
     * This method read the content that contains in file
     *
     * @param file the file
     * @return file content
     */
    private static String read(final File file) {
        // @Note Check if the file not exists
        if (!file.exists()) {
            return null;
        }

        try (final BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            final StringBuilder sb = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }

            // @Note Check if the StringBuilder length is bigger than 0, to can remove the last character that
            // represents '\n' that is add on the last while-loop
            if (sb.length() > 0) {
                // @Note Delete the last character that contains in StringBuilder that represents '\n' that is add
                // on the last while-loop
                sb.deleteCharAt(sb.length() - 1);
            }

            return sb.toString();
        } catch (IOException e) {
            error("An error occured when read file content in path \"%\". (Report to an administrator).\n", file.getAbsolutePath());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Insert Editable Text,
     * This method insert an editable text to console input
     *
     * @param line the line
     */
    private static void insertEditableLine(final String line) {
        char[] charArray = line.toCharArray();

        for (final char c : charArray) {
            final int keyCode = KeyEvent.getExtendedKeyCodeForChar(c);

            // @Note Make the press and release from Robot
            robot.keyPress(keyCode);
            robot.keyRelease(keyCode);
        }
    }

    private static void copyFile(final File src, final File dest, final boolean preserveFileDate) {
        // @Note Check if the source file represents an directory then, the method that uses on copy a directory is
        // doCopyDirectory that copies the all contents that contains in the source directory to destination directory
        if (src.isDirectory()) {
            doCopyDirectory(src, dest, preserveFileDate);
        }
        // @Note Check if the source file represents a file then, the all content that contains in the source file is
        // copied to destination file
        else {
            doCopyFile(src, dest, preserveFileDate);
        }
    }

    /**
     * Do Copy Directory,
     * This method makes the copy from a directory
     *
     * @Note This method represents an implementation from Apache Commons Lang from GitHub
     * https://github.com/apache/commons-io/blob/master/src/main/java/org/apache/commons/io/FileUtils.java
     *
     * @param src the source directory
     * @param dest the destination directory
     * @param preserveFileDate this represents if the all files that contains in destination files that are copied from
     *                         source directory has the date from source file
     */
    private static void doCopyDirectory(final File src, final File dest, final boolean preserveFileDate) {
        if (src == null) throw new NullPointerException("src");
        if (dest == null) throw new NullPointerException("dest");

        // @Note Check if the source file is a directory
        if (!src.isDirectory()) {
            error("Source file in path \"%\" must be a directory.\n", src.getAbsolutePath());

            parseCommand(); // Recursive
            return;
        }

        // @Note Check if the destination file is a directory
        if (!dest.isDirectory()) {
            error("Destination file in path \"%\" must be a directory.\n", dest.getAbsolutePath());

            parseCommand(); // Recursive
            return;
        }

        // @Note Check if the destiantion directory exists
        if (!dest.exists()) {
            error("Destination directory in path \"%\" not exists.\n", dest.getAbsolutePath());

            parseCommand(); // Recursive
            return;
        }

        // @Note List the all files that contains in the source directory
        for (final File srcFile : src.listFiles()) {
            final File destFile = new File(dest, srcFile.getName());
            // @Note Check if the source file is a directory then, copy the all files that contains in the directory
            // to the destination directory
            if (srcFile.isDirectory()) {
                doCopyDirectory(srcFile, destFile, preserveFileDate);
            }
            // @Note Check if the source file that contains in the source directory is a file then, copy the file
            // to destination directory
            else {
                doCopyFile(srcFile, destFile, preserveFileDate);
            }
        }

        if (preserveFileDate) {
            dest.setLastModified(src.lastModified());
        }
    }

    /**
     * Do Copy File,
     * This method makes the copy from a file
     *
     * @Note This method represents an implementation from Apache Commons Lang Library from GitHub
     * https://github.com/apache/commons-io/blob/master/src/main/java/org/apache/commons/io/FileUtils.java
     *
     * @param src the source file
     * @param dest the destination file
     * @param preserveFileDate this represents if the destination file has the last modification date from source file
     */
    private static void doCopyFile(final File src, final File dest, final boolean preserveFileDate) {
        if (src == null) throw new NullPointerException("src");
        if (dest == null) throw new NullPointerException("dest");

        // @Note Check if the source is a directory
        if (src.isDirectory()) {
            error("Source file in path \"%\" can not be a directory.\n", src.getAbsolutePath());

            parseCommand(); // Recursive
            return;
        }

        // @Note Check if the destination file not exists then,
        // makes the destination file
        if (!dest.exists()) {
            try {
                dest.createNewFile();
            } catch (IOException e) {
                error("An error occured when make the destination file in path \"%\". (Report to an administrator)\n", dest.getAbsolutePath());
                parseCommand(); // Recursive
                return;
            }
        }

        try (final FileInputStream fis = new FileInputStream(src)) {
            final FileChannel in = fis.getChannel();

            final FileOutputStream fos = new FileOutputStream(dest);
            final FileChannel out = fos.getChannel();

            final long size = in.size();

            // @Todo Change this variable to a static final variable (constant)
            final long maxBufferSize = 1024 * 1024 * 30; // 30 MB

            long pos = 0;
            long count = 0;

            while (pos < size) {
                final long remain = size - pos;
                count = remain > maxBufferSize ? maxBufferSize : remain;

                final long bytesCopied = out.transferFrom(in, pos, count);

                // @Note Check if has not more bytes to copy
                if (bytesCopied == 0) {
                    break;
                }

                pos += bytesCopied;
            }
        } catch (IOException e) {
            error("An error occured when make the copy from source file \"%\" to destination file \"%\". (Report to an administrator)\n", src.getAbsolutePath(), dest.getAbsolutePath());
            e.printStackTrace();
        }

        final long srcLen = src.length();
        final long destLen = dest.length();

        // @Note Check if the source file length is different from destination file length,
        // if the length is different then, this represents that the copy from content from source file
        // to destination file fails
        if (srcLen != destLen) {
            error("An error occured when copy source file in path \"%\" to destination file path \"%\", copy content fails to copy.", src.getAbsolutePath(), dest.getAbsolutePath());

            parseCommand(); // Recursive
            return;
        }

        if (preserveFileDate) {
            dest.setLastModified(src.lastModified());
        }
    }

    /**
     * Spacing,
     * This method creates a string that represents a spacing
     * that has the length relative a "relativeString" argument
     * length
     *
     * @param relativeString the relative string
     * @return a spacing string
     */
    private static String spacing(final String relativeString, final int spacing) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < spacing - relativeString.length(); i++) {
            sb.append(" ");
        }
        return sb.toString();
    }
}
