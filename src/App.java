package app;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * App, class,
 * This represents the main class from Application Manager,
 * This represents an app that handles files and folders in general
 *
 * @author biologysiwell (23/04/2018 20:46)
 * @version 1.1.3 (14/05/2018 15:33)
 * @since 0.1
 */
public final class App {

    public static void main(String[] args) {
        new App();
    }

    // -- Frame Contents --

    private static final int WIDTH = 1000;
    private static final int HEIGHT = 500;

    /**
     * Destroy App Flag, flag,
     * This represents the destruction application flag, that marks in application that the application will be
     * destructed
     * @since 1.1.3
     */
    private static final int DESTROY_APP_FLAG = 0x1;

    /**
     * Ignore Database Save, flag,
     * This represents the ignore database save flag that makes when the application is closed the application not
     * makes the save from the application to storage file
     * @since 1.1.3
     */
    private static final int IGNORE_DATABASE_SAVE = 0x2;

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    private final JFrame frame;
    private final JTextField commandInputField;
    private final JTextArea commandArea;
    private final JScrollPane commandScrollPane;

    private final Map<String, String> keys;

    /**
     * Flags, flag,
     * This variable represents the flag that are enabled in the application, this makes the controls for some commands
     * and operations that makes the control from the application
     * @since 1.1.3
     */
    private int flags;

    // @Note This class represents the Test class from the Application,
    // this class implements the Application Manager to a JFrame
    public App() {
        // @Note Pre-initialization
        this.frame = new JFrame("Application Manager v1.1.3");
        this.commandInputField = new JTextField("");
        this.commandArea = new JTextArea("");
        this.commandScrollPane = new JScrollPane(this.commandArea);

        this.frame.setVisible(true);
        this.frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.frame.setLayout(null);
        this.frame.setSize(App.WIDTH, App.HEIGHT);
        this.frame.setResizable(false);
        // @Todo Set to the application frame set the location to the center from the window

        this.commandInputField.setSize(WIDTH - 32 /* (int) ((50 / 4) * 2) + 8 */, 20);
        this.commandInputField.setLocation(12 /* (int) (50 / 4) */, HEIGHT - (50 + this.commandInputField.getHeight()));
        this.commandInputField.addActionListener(new DispatchCommandEvent());
        this.commandInputField.setFont(new Font("Consolas", 0, 12));

        // this.commandArea.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.decode("#AAAAAA"), 1), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        // this.commandArea.setSize(WIDTH - 50, HEIGHT - 100);
        // this.commandArea.setLocation(12 /* (int) (50 / 4) */, 10);
        this.commandArea.setEditable(false);
        this.commandArea.setVisible(true);
        this.commandArea.setFont(new Font("Consolas", 0, 14));

        this.commandScrollPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.decode("#AAAAAA"), 1), BorderFactory.createEmptyBorder(1, 1, 1, 1)));
        this.commandScrollPane.setBounds(12 /* (int) (50 / 4) */, 10, WIDTH - 32 /* (int) ((50 / 4) * 2) + 8 */, HEIGHT - 100);

        this.frame.add(this.commandInputField);
        this.frame.add(this.commandScrollPane);

        // @Note This method must be invoked again to update the all components that are added on the JFrame, otherwise
        // the components is not show in the JFrame
        //      -biologyiswell, 13 May 2018
        this.frame.setVisible(true);

        this.keys = new LinkedHashMap<>();

        // @Note Initialization
        // @Note This method makes that a hook to the method "saveApplicationDatabase" to be executed when the console.
        // @Note (14/05/2018 19:07) Now the method has been changed form the "saveApplicationDatabase" to "exit",
        // because the exit method represents a safely and configurable method
        // is closed, -biologyiswell 08 May 2018
        Runtime.getRuntime().addShutdownHook(new Thread(() -> this.exit(0, (this.flags & IGNORE_DATABASE_SAVE) != 0)));

        this.loadApplicationDatabase();
        this.printWelcome();
    }

    /**
     * DispatchCommandEvent, class,
     * This represents the class that handle the dispatch command event,
     * that handle the dispatch command updates
     *
     * @author biologyiswell (07/05/2018 17:21)
     * @since 1.1
     */
    class DispatchCommandEvent implements ActionListener {
        private int line = 0;

        @Override
        public void actionPerformed(ActionEvent e) {
            // @Note Parse the command and clear input field
            App.this.parseCommand(App.this.commandInputField.getText());
            App.this.commandInputField.setText("");
        }
    }

    // -- End Frame Contents --

    /**
     * Parse Command,
     * This method parse the command input from JTextField, that represents the JComponent that contains the input
     * that represents the command, that will be parse
     *
     * @param input input string, this represents the input string that is get from the JTextField that will be parse
     *              by the method
     * @since 1.1
     */
    private void parseCommand(final String input) {
        // @Note Check if the input is null, case if the input is null the error represents an internal error
        if (input == null) {
            this.commandArea.setText("");
            this.commandArea.setForeground(Color.RED);
            errorf("An internal error occured \"%\". (Report to an administrator)\n", "input is null");

            throw new NullPointerException("an internal error occured \"input is null \". (Report to an administrator)");
        }

        final String[] args = input.split(" ");
        final String command = args[0];

        // @Note Print the help commands
        if (checkCommand(command, "help")) {
            this.printHelpCommands();
            return;
        }
        // @Note Prints a custom message using echo command
        else if (checkCommand(command, "echo")) {
            final String echoMessage = join(1, args.length, " ", args);

            printf(echoMessage + "\n");
            return;
        }
        // @Note List the all keys that are registered in the application
        else if (checkCommand(command, "list")) {
            // @Note Check if the keys map size is equals 0
            if (this.keys.size() == 0) {
                printf("No have keys to be list.\n");
                return;
            }

            printf("\n");
            printf("Listed keys\n");
            printf("Size: %\n", this.keys.size());
            printf("\n");
            int index = 1;
            for (final Map.Entry<String, String> entry : this.keys.entrySet()) {
                System.out.println(entry.getValue().length());
                printf("%. % -> %\n", index++, entry.getKey(), entry.getValue());
            }
            return;
        }
        // @Note Register a key that makes the easy to access the application by path
        else if (checkCommand(command, "register", "reg")) {
            // @Note Check if the register command has the necessary arguments (<key> <path>)
            if (args.length < 3) {
                this.usage("register <key> <path>");
                return;
            }

            // @Note The variables represents the key and the path input by the console input
            final String key = args[1];
            final String path = args[2];

            // @Note Check if the key is null or empty
            if (key == null || key.isEmpty()) {
                printf("The key name must be declared.\n");
                return;
            }

            // @Note Check if the path is null or empty
            if (path == null || path.isEmpty()) {
                printf("The path must be declared.\n");
                return;
            }

            // @Note Check if the key has been registered
            if (this.keys.containsKey(key)) {
                printf("The key \"%\" already registered.\n", key);
                return;
            }

            // @Note Register the key with path
            this.registerKey(key, path);
            printf("Key \"%\" has been registered with path \"%\".\n", key, path);
            return;
        }
        // @Note Deletes a key from the application
        else if (checkCommand(command, "delete", "del")) {
            // @Note Check if the delete command has the necessary arguments (<key>)
            if (args.length < 2) {
                this.usage("delete <key>");
                return;
            }

            final String key = args[1];

            // @Note Check if the key is null or empty
            if (key == null || key.isEmpty()) {
                printf("The key must be declared.\n");
                return;
            }

            // @Note Check if the key is already registered by the application
            if (!this.keys.containsKey(key)) {
                printf("The key \"%\" not registered on the application.\n", key);
                return;
            }

            // @Note Deletes the key from application
            this.deleteKey(key);
            printf("Key \"%\" has been removed from the application.\n", key);
            return;
        }
        // @Note Runs a key by path
        else if (checkCommand(command, "run")) {
            // @Note Check if the command has the necessary arguments (<key>)
            if (args.length < 2) {
                this.usage("run <key>");
                return;
            }

            final String key = args[1];

            // @Note Check if the key is null or empty
            if (key == null || key.isEmpty()) {
                printf("The key must be declared\n");
                return;
            }

            // @Note Check if the key is registered in the application
            if (!this.keys.containsKey(key)) {
                printf("The key \"%\" not registered in the application.\n", key);
                return;
            }

            // @Note This method run the key
            this.runKey(key);
            printf("Running %...\n", key);
            return;
        }
        // @Note Edits the key path that is registered in the application
        else if (checkCommand(command, "edit")) {
            // @Note Check if the edit command has the necessary arguments (<key> <new path>)
            if (args.length < 3) {
                this.usage("edit <key> <new path>");
                return;
            }

            final String key = args[1];
            final String newPath = args[2];

            // @Note Check if the key is null or empty
            if (key == null || key.isEmpty()) {
                printf("The key must be declared.\n");
                return;
            }

            // @Note Check if the key is registered in the application
            if (!this.keys.containsKey(key)) {
                printf("The key \"%\" not registered in the application.\n", key);
                return;
            }

            // @Note Check if the new path is null or empty
            if (newPath == null || newPath.isEmpty()) {
                printf("The new path must be declared.\n");
                return;
            }

            // @Note Assign the new path to the key in keys map
            this.keys.put(key, newPath);
            printf("The key \"%\" has path changed to \"%\".\n", key, newPath);
            return;
        }
        // @Note Creates a directory
        else if (checkCommand(command, "mkdir")) {
            // @Note Check if the command to create the directory has the necessary arguments (<path>)
            if (args.length < 2) {
                this.usage("mkdir [ -rc ] <paths...>");
                return;
            }

            // @Note This boolean represents if the flag "recreates" has enabled, this flag makes that the if the file
            // exists then deletes the file and re-create the file
            final boolean recreates = this.hasFlag(args, "-rc");
            final String path = this.join(recreates ? 2 : 1, args.length, " ", args);

            // @Note Check if the path is null or empty
            if (path.isEmpty()) {
                printf("The path must be declared.\n");
                return;
            }

            final String[] multiplesPath = path.split(",");

            // @Note For-each loop by each current path that contains in the multiples path separated by separator ","
            for (final String currentPath : multiplesPath) {
                final File file = new File(currentPath.trim());

                // @Note Check if the file not exists
                if (!file.exists()) {
                    file.mkdirs();
                }
                // @Note Check if the file exists and is to recreates
                else if (recreates) {
                    this.delete(file);
                    file.mkdirs();
                } else {
                    printf("The directory in path \"%\" can not be created because already exists. (To bypass this use the flag \"-rc\" in the command)\n", file.getAbsolutePath());

                    // @Note Do not use "return" and "break" here, with this the for-each loop is broke
                    continue;
                }

                printf("The directory in path \"%\" has been created.\n", file.getAbsolutePath());
            }
            return;
        }
        // @Note Makes a file
        else if (checkCommand(command, "mkfile")) {
            // @Note Check if the command to makes a file has the necessary arguments
            if (args.length < 2) {
                this.usage("mkfile [ -rc ] <paths...>");
                return;
            }

            // @Note This boolean represents if the flag "recreates" has enabled, this flag makes that the if the file
            // exists then deletes the file and re-create the file
            final boolean recreates = this.hasFlag(args, "-rc");
            final String path = this.join(recreates ? 2 : 1, args.length, " ", args);

            // @Note Check if the path is null or empty
            if (path.isEmpty()) {
                printf("The path must be declared.\n");
                return;
            }

            final String[] multiplesPath = path.split(",");

            // @Note For-each loop by each current path that contains in the multiples path separated by separator ","
            for (final String currentPath : multiplesPath) {
                final File file = new File(currentPath.trim());
                try {
                    // @Note Check if the file not exists
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    // @Note Check if the file exists and if the command has the recreates flag
                    else if (recreates) {
                        this.delete(file);
                        file.createNewFile();
                    } else {
                        printf("The file in path \"%\" can not be created because already exists. (To bypass this use the flag \"-rc\" in the command)\n", file.getAbsolutePath());

                        // @Note Do not use "return" and "break" here, with this the for-each loop is broke
                        continue;
                    }
                } catch (Exception e) {
                    errorf("An internal error occured when executes the command \"mkfile\".\n");
                    e.printStackTrace();
                }

                printf("The file in path \"%\" has been created.\n", file.getAbsolutePath());
            }

            return;
        }
        // @Note Makes the delete from the file
        else if (checkCommand(command, "deletefile", "delfile")) {
            // @Note Check if the delete file command has the necessary arguments (<path>)
            if (args.length < 2) {
                this.usage("deletefile <paths...>");
                return;
            }

            final String path = this.join(1, args.length, " ", args);

            // @Note Check if the path is null or empty
            if (path.isEmpty()) {
                printf("The path must be declared.\n");
                return;
            }

            final String[] multiplesPath = path.split(",");

            // @Note For-each loop from the multiples path string array
            for (final String currentPath : multiplesPath) {
                // @Note Use the trim method to remove the around blankspaces, because the separator can be has a
                // blankspace
                final File file = new File(currentPath.trim());

                // @Note Check if the file exists to delete
                if (!file.exists()) {
                    printf("The file in path \"%\" can not be deleted because not exists.\n", file.getAbsolutePath());

                    // @Note Do not use "return" and "break" here, with this the for-each loop is broke
                    continue;
                }

                // @Note Delete the file or directory
                this.delete(file);

                printf("The % in path \"%\" has been deleted.\n", file.isDirectory() ? "directory" : "file", file.getAbsolutePath());
            }

            return;
        }
        // @Note Reads the content from a file
        else if (checkCommand(command, "read")) {
            // @Note Check if the command has the necessary arguments
            if (args.length < 2) {
                this.usage("read [ -in ] <path>");
                return;
            }

            // @Note This represents the "input display" flag that is represented by "-in", this flag makes that the
            // read content that is read from the file is write in the console input
            final boolean inputDisplay = this.hasFlag(args, "-in");
            final String path = this.join(inputDisplay ? 2 : 1, args.length, " ", args);

            // @Note Check if the path is empty
            if (path.isEmpty()) {
                printf("The path must be declared.\n");
                return;
            }

            final File file = new File(path);

            // @Note Check if the file is a directory
            if (file.isDirectory()) {
                printf("The file in path \"%\" can not be read because is a directory.\n", path);
                return;
            }

            // @Note This represents the content that is read from the file
            final String content = this.read(file);

            printf("\n");
            printf("File in path \"%\" content: \n", path);
            printf("%", content);
            printf("\n");

            // @Note Check if the commnad has the input flag then, if has the input display flag the content read
            // from file is set in the console input
            if (inputDisplay) {
                this.commandInputField.setText(content);
            }
            return;
        }
        // @Note List the all files and directories that contains into the directory
        else if (checkCommand(command, "dir")) {
            // @Note Check if the command dir has the necessary arguments
            if (args.length < 2) {
                this.usage("dir <path>");
                return;
            }

            final String path = this.join(1, args.length, " ", args);

            // @Note Check if the path is null or empty
            if (path == null || path.isEmpty()) {
                printf("The path must be declared.\n");
                return;
            }

            final File file = new File(path);

            // @Note Check if the file is directory to can list the files that contains in directory
            if (!file.isDirectory()) {
                printf("The file can not be listed because is not a directory.\n");
                return;
            }

            // @Note This array represents the all files that contains in the directory
            final File[] listFiles = file.listFiles();

            printf("\n");
            printf("  Listed directory: %.\n", path);
            printf("  Listed files: %.\n", listFiles.length);
            printf("\n");
            // @Note List the all files that contains in the directory
            for (final File fileIn : listFiles) {
                printf("% % % %\n", DATE_FORMAT.format(fileIn.lastModified()) + this.spacing(2), (fileIn.isDirectory() ? "<DIR>" + this.spacing(1) : this.spacing(5)), fileIn.isDirectory() ? "" : (double) (fileIn.length() / 1024d) + "kb", fileIn.getName());
            }
            return;
        }
        // @Note Clear screen
        else if (checkCommand(command, "clear")) {
            this.commandArea.setText("");
            this.printWelcome();
            return;
        }
        // @Note Makes the destroy from the application
        else if (checkCommand(command, "destroy")) {
            // @Note This condition makes the check from the destroy command, this condition checks if the application
            // flags has not the destruction application flag enabled
            if ((this.flags & DESTROY_APP_FLAG) == 0) {
                this.flags |= DESTROY_APP_FLAG;

                printf("To confirm the process type \"destroy\", again.\n");
                return;
            }

            // @Note This not serves more, but to make the operation more safely is required to remove the destruction
            // application flag from the application flags, but when this command is executed the application has not
            // save the application to storage file, to do this need be add the flag "IGNORE_DATABASE_SAVE" to the
            // application flags
            //      -biologyiswell, 14 May 2018
            this.flags &= ~DESTROY_APP_FLAG;
            this.flags |= IGNORE_DATABASE_SAVE;

            printf("Destroying app...\n");
            this.destroyApp();
            return;
        }
        else if (checkCommand(command, "editfile")) {
            // @Note Check if the command has the necessary arguments
            if (args.length < 2) {
                this.usage("editfile <file path>");
                return;
            }

            final String path = this.join(1, args.length, " ", args);

            // @Note Check if the path is empty
            if (path.isEmpty()) {
                printf("The path must be declared.\n");
                return;
            }

            final File file = new File(path);

            // @Note Check if the file is not exists
            if (!file.exists()) {
                printf("The file in path \"%\" can not be edited, because not exists.\n", path);
                return;
            }

            // @Note Check if the file represents a directory
            if (file.isDirectory()) {
                printf("The file in path \"%\" can not edited, because is a directory.", path);
                return;
            }

            printf("Editing file %...\n", path);

            final JFrame efFrame = new JFrame("Edit file: " + path);
            efFrame.setSize(1280, 720);
            efFrame.setLayout(null);
            efFrame.setVisible(true);
            efFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            efFrame.setResizable(false);

            final JTextArea efTextArea = new JTextArea();
            efTextArea.setFont(new Font("Consolas", 0, 14));

            final JScrollPane efScrollPane = new JScrollPane(efTextArea);
            efScrollPane.setBounds(12 /* (int) (50 / 4 )*/, 10, efFrame.getWidth() - 32 /* (int) ((50 / 4) * 2) + 8 */, efFrame.getHeight() - 100);
            efScrollPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.decode("#AAAAAA"), 1), BorderFactory.createEmptyBorder(1, 1, 1, 1)));

            final JButton efButton = new JButton("Save file");
            efButton.setSize(150, 50);
            efButton.setLocation((efFrame.getWidth() / 2) - (efButton.getWidth() / 2), efFrame.getHeight() - (efButton.getHeight() + (efButton.getHeight() / 2) + 8));
            efButton.addActionListener(e -> {
                // @Note This method makes that the append text is false, because the content that is displayed on the
                // JTextArea represents the current content from the file
                this.write(file, efTextArea.getText(), false);
                printf("File in path \"%\" has been edited.\n", path);

                // @Note Close the edit file frame
                efFrame.dispose();
            });

            efFrame.add(efButton);
            efFrame.add(efScrollPane);
            efFrame.setVisible(true);

            // @Note Read the content that contains in the file
            final String content = this.read(file);

            efTextArea.setText(content);
            return;
        }

        // @Note If the code arrives here this means that the command is not parse by the command checkers,
        // then the command is not found by the application
        printf("Command has not found. Try again!\n");
    }

    // Storage

    /**
     * Load Application Database, method,
     * This method load the all application database
     *
     * @since 1.1
     */
    private void loadApplicationDatabase() {
        final File storageFile = this.getStorageFile();

        // @Note This condition makes the check that the storage is not exists and the storage is not created after
        // from the method from file "mkdirs"
        if (!storageFile.exists()) {
            try {
                // @Note This condition check if the storage has been created
                final boolean isStorageCreated = storageFile.createNewFile();
                if (!isStorageCreated) {
                    errorf("An internal error occured when create storage database file \"%\". (Report to an administrator)\n", storageFile.getAbsolutePath());
                    exit(0);

                    return;
                }
            } catch (Exception e) {
                errorf("An internal error occured when create storage database file \"%\" (with exception). (Report to an administrator)\n", storageFile.getAbsolutePath());
                e.printStackTrace();

                exit(0);
                return;
            }

            // @Note Do not remove this return, this return is here because to not continue the process to load the
            // informations from the database, like that the database is created now the database not contains any
            // keys to be loaded
            return;
        }

        // @Note Read the content from the storage file and split the content in lines
        final String[] lines = this.read(storageFile).split(System.lineSeparator());
        for (final String line : lines) {
            // @Note Check if the line is empty then, the process of parse can be continue
            if (line.isEmpty() || line.equals("\n")) {
                continue;
            }

            // @Note In this string array contains the key and path
            final String[] keyAndValue = line.split(" ");
            this.keys.put(keyAndValue[0], keyAndValue[1]);
        }

        System.out.println("Application has been loaded.");
    }

    /**
     * Save Application Database,
     * This method save the all informations from application into the storage
     *
     * @since 1.1
     */
    private void saveApplicationDatabase() {
        final StringBuilder sb = new StringBuilder();

        // @Note For-each loop all entries that contains in entry set
        for (final Map.Entry<String, String> entry : this.keys.entrySet()) {
            sb.append(entry.getKey()).append(' ').append(entry.getValue()).append(System.lineSeparator());
        }

        // @Note Write the content from keys map into the storage file
        this.write(this.getStorageFile(), sb.toString(), false);
        System.out.println("Application has been saved.");
    }

    // Methods

    /**
     * Run Key, method,
     * This method run the key getting the path that the key has, this method already check if the key is null, and if
     * the key is null the method throw a NullPointerException, but if the path is null the method not execute anything
     *
     * @param key the key that will be get path to run the key
     */
    private void runKey(final String key) {
        // @Note Check if the key is null
        if (key == null) {
            errorf("An internal error occured when run a key \"%\". (Report to an administrator)\n", "key is null");
            exit(0);

            throw new NullPointerException("an internal error occured when run a key (key is null)");
        }

        // @Note This represents the path that is registered by the application
        final String path = this.keys.get(key);

        // @Note Check if the path is null
        if (path == null) {
            return;
        }

        final int pathSeparateFileIndex = path.lastIndexOf("\\");
        final File filePath = new File(path.substring(0, pathSeparateFileIndex));
        final String fileName = path.substring(pathSeparateFileIndex + 1, path.length());

        System.out.println("File Path: " + filePath.getAbsolutePath());
        System.out.println("File Name: " + fileName);

        // @Note This method run a operational system command
        runOsCommand(filePath, "cmd", "/c", "start " + fileName);
    }

    /**
     * Register Key,
     * This method register the key and value into the keys map, this method already check if the key and the path is
     * null, in the both cases if the key and path is null the method throw a NullPointerException
     *
     * @param key the key
     * @param path the path
     * @return true, if the key has been registered, otherwise false
     * @since 0.1
     * @lastChange 1.1 -> Recode the register key method to apply to the new configurations about the new console
     * design
     */
    private void registerKey(final String key, final String path) {
        // @Note Check if the key is null
        if (key == null) {
            errorf("An internal error occured when register key \"%\". (Report to an administrator)\n", "key is null");
            exit(0);

            throw new NullPointerException("an internal error occured when register key (key is null)");
        }

        // @Note Check if the path is null
        if (path == null) {
            errorf("An internal error occured when register key \"%\". (Report to an administrator)\n", "path is null");
            exit(0);

            throw new NullPointerException("an internal error occured when register key (path is null)");
        }

        this.keys.put(key, path);
    }

    /**
     * Delete Key, method,
     * This method deletes the key from the application
     *
     * @param key the key
     */
    private void deleteKey(final String key) {
        // @Note Check if the key is null
        if (key == null) {
            errorf("An internal occured when deletes key \"%\". (Report to an administrator)\n", "key is null");
            exit(0);

            throw new NullPointerException("an internal error occured when deletes key (key is null)");
        }
        
        this.keys.remove(key);
    }

    /**
     * Destroy App, method,
     * This method destroy the application database
     *
     * @return true, if the destruction from the application is completed, otherwise false
     */
    private void destroyApp() {
        try {
            // @Note Deletes the storage file
            this.delete(this.getStorageFile());
            this.keys.clear();

            printf("Application has been destroyed.\n");
            printf("Application will be shutdown in 3 seconds.");

            // @Note This method exit the application but not save the application in database
            exit(5000, true);
        } catch (Exception e) {
            errorf("An internal error occured when destroy the application. (Report to an administrator)\n");
        }
    }

    // Utils

    /**
     * Delete, method,
     * This method deletes the file, this method check if the method is a directory then, deletes the all files that
     * contains in the directory and the directory, otherwise if the file represents a file only delete the file, this
     * represents a safe method that deletes the files and directory, do not use {@link File#delete()}
     *
     * @param file the file that will be delete
     */
    private void delete(final File file) {
        // @Note Check if the file is null
        if (file == null) {
            return;
        }

        // @Note Check if the file is a directory or if the file is a file, this check is important to make the
        // deletion from the file
        if (file.isDirectory()) {
            // @Note Make the for-each loop from the all files that contains in the directory, and delete the files
            for (final File fileIn : file.listFiles()) {
                this.delete(fileIn);
            }

            // @Note After from delete from the all files that contains in the directory, now deletes the directory
            file.delete();
        } else {
            file.delete();
        }
    }

    /**
     * Has Flag, method,
     * This method check if the source string array contains the string flag
     *
     * @param src the source string array
     * @param flag the flag that will check if contains in the source string array
     * @return true, if the flag contains in the source string array, otherwise false
     */
    private boolean hasFlag(final String[] src, final String flag) {
        for (final String string : src) {
            if (string.equalsIgnoreCase(flag)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Usage, method,
     * This method prints to console output to how that use the command, this method is used to prints the how that
     * the input from command need be put in the console input
     *
     * @param usage the usage of command
     * @since 0.1
     */
    private void usage(final String usage) {
        printf("Incorrect command input. Use: %.\n", usage);
    }

    /**
     * Check Command, method,
     * This method check if the string input represents an each from the commands, this method returns true if the
     * string input represents a string that contains in the commands varargs
     *
     * @param input the input
     * @param commands the commands that will be checked
     * @return true if the input has on the commands
     */
    private boolean checkCommand(final String input, final String... commands) {
        for (final String command : commands) {
            if (command.equalsIgnoreCase(input)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Print Format, method,
     * This method prints a formatted message, a formatted message is a message that contains variable that can be
     * replaced by arguments, in this case, the variable is represented by '%' and the arguments is represented by the
     * varargs of "args" that is an argument from argument list from method
     *
     * @param printWithDate if true, the method prints a message with the current date
     * @param message the message that will be print to console output
     * @param args the argument list that will be replaced on the message, if the message contains variables
     * @since 0.1
     * @lastChange 1.1
     */
    private void printf(final boolean printWithDate, final String message, final Object... args) {
        // @Note This condition check if the lines overs the maximum height
        // from the JTextArea, this represents a fix from a bug about JScrollPane
        // that not scrolling the JTextArea
        //      -biologyiswell, 07 May 2018
//        if (this.commandArea.getLineCount() >= 25) {
//            final String commandAreaText = this.commandArea.getText();
//            this.commandArea.setText(commandAreaText.substring(commandAreaText.indexOf('\n'), commandAreaText.length()).trim() + "\n");
//        }

        // @Note To make the process more faster when the arguments from string is null or arguments has length 0,
        // this condition makes that the print to console output prints the message
        if (args == null || args.length == 0) {
            this.commandArea.append((printWithDate ? "[" + DATE_FORMAT.format(System.currentTimeMillis()) + "] " : "") + message);
        } else {
            final StringBuilder sb = new StringBuilder();

            int k = 0;
            for (char c : message.toCharArray()) {
                // @Note Check if the char represents the variable to be replaced by argument
                sb.append(c == '%' ? args[k++] : c);
            }

            this.commandArea.append((printWithDate ? "[" + DATE_FORMAT.format(System.currentTimeMillis()) + "] " : "") + sb.toString());
        }

        // @Note This method makes that the vertical scrollbar to follows the displaying from the current content
        this.commandScrollPane.getVerticalScrollBar().setValue(this.commandScrollPane.getVerticalScrollBar().getMaximum());
    }

    /**
     * Print Format, method,
     * This method prints a formatted message, a formatted message is a message that contains variable that can be
     * replaced by arguments, in this case, the variable is represented by '%' and the arguments is represented by the
     * varargs of "args" that is an argument from argument list from method
     *
     * @see {@link #printf(boolean, String, Object...)}
     * @param message the message that will be print to console output
     * @param args the argument list that will be replaced on the message, if the message contains variables
     * @since 0.1
     * @lastChange 1.1
     */
    private void printf(final String message, final Object... args) {
        printf(true, message, args);
    }

    /**
     * Error Format, method,
     * This method prints a formatted error message, a formatted error message is a message that contains variable that
     * can be replaced by aguments, in this case, the variable is represented by '%' and the arguments is represented by
     * the varargs of "args" that is an argument from argument list from method, the error message makes that the
     * message contains "[ERROR]"
     *
     * @see {@link #printf(boolean, String, Object...)}
     * @param printWithDate if true, the method prints a message with the current date
     * @param message the message that will be print to console output
     * @param args the argument list that will be replaced on the message, if the message contains variables
     * @since 0.1
     * @lastChange 1.1
     */
    private void errorf(final boolean printWithDate, final String message, final Object... args) {
        this.commandArea.setForeground(Color.RED);
        this.commandInputField.setEditable(false);

        printf(printWithDate, "[ERROR] " + message, args);
    }

    /**
     * Error Format, method,
     * This method prints a formatted error message, a formatted error message is a message that contains variable that
     * can be replaced by aguments, in this case, the variable is represented by '%' and the arguments is represented by
     * the varargs of "args" that is an argument from argument list from method, the error message makes that the
     * message contains "[ERROR]"
     *
     * @see {@link #printf(boolean, String, Object...)}
     * @see {@link #errorf(boolean, String, Object...)}
     * @param message the message that will be print to console output
     * @param args the argument list that will be replaced on the message, if the message contains variables
     * @since 0.1
     * @lastChange 1.1
     */
    private void errorf(final String message, final Object... args) {
        printf(true, "[ERROR] " + message, args);
    }

    /**
     * Print Welcome, method,
     * This method print the welcome message when the application is launch
     *
     * @since 1.1
     */
    private void printWelcome() {
        printf("\n");
        printf("  Application Manager v1.1\n");
        printf("  Type \"help\" to see commands.\n");
        printf("\n");
    }

    /**
     * Print Help Commands, method,
     * This method print the all help commands and flags that contains in the Application
     *
     * @since 0.1
     */
    private void printHelpCommands() {
        printf("\n");
        printf("Commands: \n");
        printf(" [ COMMAND NAME ], [ COMMAND ALIASES ] [< COMMAND ARGUMENTS >] [[ ARBITRARY FLAGS ]] - [ COMMAND DESCRIPTION ]\n");
        printf(" > clear                                - Clear screen.\n");
        printf(" > copy <from path> <to path>           - Copy a file or directory to a new path.\n");
        printf(" > delete, del <key>                    - Delete a key that is registered.\n");
        printf(" > deletefile, delfile <paths...>       - Deletes a file.\n");
        printf(" > destroy                              - Delete Application Manager Database. (!!!!)\n");
        printf(" > dir <file path> [file index]         - List the all files that contains in directory.\n");
        printf(" > edit <key> <new path>                - Edit a value from a key.\n");
        printf(" > editfile <file path>                 - Edit file content.\n");
        printf(" > list                                 - List all registered keys.\n");
        printf(" > mkdir [ -rc ] <paths...>             - Make a directory.\n");
        printf(" > mkfile [ -rc ] <paths...>            - Make a file.\n");
        printf(" > read [ -in ] <file path>             - Read file content.\n");
        printf(" > register, reg <key> <path>           - Register a path by key.\n");
        printf(" > run <key>                            - Run an application.\n");
        printf("Flags: \n");
        printf(" [ FLAG NAME ], [ FLAG FUNCTION ] (To display a flag in a command uses prefix \"-\"\n");
        printf(" > a                                    - Append content from file with new content.\n");
        printf(" > i                                    - Show informations about process.\n");
        printf(" > rc                                   - Re-create file if exists in a process.\n");
        printf(" > in                                   - Input display information.\n");
        printf("\n");
    }

    /**
     * Get Storage File, method,
     * This method returns the storage file that represents the batchs, that this folder saves the all batchs files
     * that are registered in the application
     *
     * @return storage file
     * @since 1.1
     */
    private File getStorageFile() {
        return new File("app.data");
    }

    /**
     * Join, method,
     * This method join the all strings that contains in the string array by a start index until an end index,
     * the join between strings has a delimiter that represents the character that separates these strings
     *
     * @param starts the starts index from the strings array
     * @param ends the ends index from the strings array
     * @param separator the separator string between the join from strings array
     * @param strings the string array that will be joined
     * @return joined strings array between a separator into an one string
     * @since 1.1
     */
    private String join(final int starts, final int ends, final String separator, final String... strings) {
        final StringBuilder sb = new StringBuilder();

        // @Note 1. Pass: Join the all strings into the StringBuilder with the separator string
        for (int i = starts; i < ends; i++) {
            sb.append(strings[i]).append(separator);
        }

        // @Note 2. Pass: Check if the StringBuilder length is bigger than 0, to check if contains character in the
        // string, because the process that contains into from the if statement need the check to can delete the
        // characters
        if (sb.length() > 0) {
            // @Note 3. Pass: Delete the last string separator that is added on the last for-loop
            sb.delete(sb.length() - separator.length(), sb.length());
        }

        return sb.toString();
    }

    /**
     * Join, method,
     * This method join the all strings that contains in the string array by a start index until an end index that is
     * represented by index 0, the join between strings has a delimiter that represents the character that separates
     * these strings
     *
     * @see {@link #join(int, int, String, String...)}
     * @param starts the starts index from the strings array
     * @param separator the separator string between the join from strings array
     * @param strings the string array that will be joined
     * @return joined strings array between a separator into an one string
     * @since 1.1
     */
    private String join(final int starts, final String separator, final String... strings) {
        return join(starts, 0, separator, strings);
    }

    /**
     * Join, method,
     * This method join the all strings that contains in the string array by a start index that is represented by index
     * 0 until an end index that is represented by index 0, the join between strings has a delimiter that represents the
     * character that separates these strings
     *
     * @see {@link #join(int, int, String, String...)}
     * @see {@link #join(int, String, String...)}
     * @param separator the separator string between the join from strings array
     * @param strings the string array that will be joined
     * @return joined strings array between a separator into an one string
     * @since 1.1
     */
    private String join(final String separator, final String... strings) {
        return join(0, 0, separator, strings);
    }

    /**
     * Copy File, method,
     * This method makes the copy file to a new destination path, the method contains the boolean value that represents
     * if is to preserve the file date
     *
     * @param src the source file that will be copied
     * @param dest the destination file that the source file will be copied
     * @param preserveFileDate if true the source file date is preserved to destination file date
     * @since 0.1
     */
    private void copyFile(final File src, final File dest, final boolean preserveFileDate) {
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
     * @param src              the source directory
     * @param dest             the destination directory
     * @param preserveFileDate this represents if the all files that contains in destination files that are copied from
     *                         source directory has the date from source file
     * @Note This method represents an implementation from Apache Commons Lang from GitHub
     * https://github.com/apache/commons-io/blob/master/src/main/java/org/apache/commons/io/FileUtils.java
     * @since 0.1
     */
    private void doCopyDirectory(final File src, final File dest, final boolean preserveFileDate) {
        if (src == null) throw new NullPointerException("src");
        if (dest == null) throw new NullPointerException("dest");

        // @Note Check if the source file is a directory
        if (!src.isDirectory()) {
            errorf("Source file in path \"%\" must be a directory.\n", src.getAbsolutePath());
            return;
        }

        // @Note Check if the destination file is a directory
        if (!dest.isDirectory()) {
            errorf("Destination file in path \"%\" must be a directory.\n", dest.getAbsolutePath());
            return;
        }

        // @Note Check if the destiantion directory exists
        if (!dest.exists()) {
            errorf("Destination directory in path \"%\" not exists.\n", dest.getAbsolutePath());
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
     * @param src              the source file
     * @param dest             the destination file
     * @param preserveFileDate this represents if the destination file has the last modification date from source file
     * @Note This method represents an implementation from Apache Commons Lang Library from GitHub
     * https://github.com/apache/commons-io/blob/master/src/main/java/org/apache/commons/io/FileUtils.java
     * @since 0.1
     */
    private void doCopyFile(final File src, final File dest, final boolean preserveFileDate) {
        if (src == null) throw new NullPointerException("src");
        if (dest == null) throw new NullPointerException("dest");

        // @Note Check if the source is a directory
        if (src.isDirectory()) {
            errorf("Source file in path \"%\" can not be a directory.\n", src.getAbsolutePath());
            return;
        }

        // @Note Check if the destination file not exists then,
        // makes the destination file
        if (!dest.exists()) {
            try {
                dest.createNewFile();
            } catch (IOException e) {
                errorf("An error occured when make the destination file in path \"%\". (Report to an administrator)\n", dest.getAbsolutePath());
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
            errorf("An error occured when make the copy from source file \"%\" to destination file \"%\". (Report to an administrator)\n", src.getAbsolutePath(), dest.getAbsolutePath());
            e.printStackTrace();
        }

        final long srcLen = src.length();
        final long destLen = dest.length();

        // @Note Check if the source file length is different from destination file length,
        // if the length is different then, this represents that the copy from content from source file
        // to destination file fails
        if (srcLen != destLen) {
            errorf("An error occured when copy source file in path \"%\" to destination file path \"%\", copy content fails to copy.", src.getAbsolutePath(), dest.getAbsolutePath());
            return;
        }

        if (preserveFileDate) {
            dest.setLastModified(src.lastModified());
        }
    }

    /**
     * Spacing, method,
     * This method makes a spaces with a size
     *
     * @param size the size of the spacing
     * @return the spacing string
     * @since 0.1
     * @lastChange 1.1
     */
    private String spacing(final int size) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size; i++) {
            sb.append(" ");
        }
        return sb.toString();
    }

    /**
     * Write, method,
     * This method write a content into a file
     *
     * @param file the file that will receives the new/append content
     * @param content the content that will be write the new/append content into the file
     * @param appendContent if the true the content that has on the argument list is append into the file content,
     *                      the current file content is read and join with the content from argument list, otherwise
     *                      the content is re-write into the file
     * @since 1.1
     */
    private void write(final File file, final String content, final boolean appendContent) {
        // @Note Check if the file is null
        if (file == null) {
            errorf("An internal error occured when write in a file \"%\". (Report to an administrator)\n", "file is null");
            exit(0);

            throw new NullPointerException("an internal error occured on write file (file is null)");
        }

        // @Note Check if the file is a directory
        if (file.isDirectory()) {
            errorf("An internal error occured when write in a file \"%\". (Report to an administrator)\n", "file is a directory");
            throw new IllegalArgumentException("an internal error occured on write file (file is a directory)");
        }

        try (final FileWriter writer = new FileWriter(file)) {
            // @Note This variable check if the write from the content into the file is by re-write or by append
            // content
            final String newContent = (appendContent ? this.read(file) : "") + content;

            // @Note This method write the new content into the file
            writer.write(newContent);
        } catch (IOException e) {
            errorf("An internal error occured when write in a file \"%\" (with exception)\n");
            exit(0);

            e.printStackTrace();
        }
    }

    /**
     * Read, method,
     * This method read the file content, this method already check if the file is null then, if the file is null the
     * method throw a NullPointerException, if the file is a directory the method thrown an IllegalArgumentException
     * that represents that the file can not be a directory, then if the file not exists then the method returns null
     *
     * @param file the file
     * @return file content into an one string
     * @since 0.1
     */
    private String read(final File file) {
        if (file == null) {
            errorf("An internal error occured when read a file content \"%\". (Report to an administrator)\n", "file is null");
            exit(0);

            throw new NullPointerException("an internal error occured on read file (file is null)");
        }

        // @Note Check if the file is a directory, then this the method can not be continue because can not read a
        // directory, this condition should be above of check if the file exists
        if (file.isDirectory()) {
            errorf("An internal error occured when read a file content \"%\". (Report to an administrator)\n", "file is a directory");
            throw new IllegalArgumentException("an internal error occured on read file (file is a directory)");
        }

        // @Note Check if the file exists
        if (!file.exists()) {
            return null;
        }

        // @Note Not initialize this StringBuilder, the file can not be read
        final StringBuilder sb;

        // @Note 1. Pass: Create the buffered reader object using "try-with-resources" that make the read from the all
        // file content
        try (final BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            sb = new StringBuilder();

            // @Note 2. Pass: Make a while-loop while that the line is assigned by the "readLine" method from the
            // buffered reader object and the line is different from null, this represents that has line to be readed
            // and the StringBuilder that is a object that has been created to append the all lines that is created
            // to make this lines into an one string
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }

            // @Note This condition checks if the StringBuilder is bigger than 0
            if (sb.length() > 0) {
                // @Note This method deletes the last character that represents the "System.lineSeparator()" that represents
                // the separator line string that is added on the last for-each loop, this method fixes a bug that makes the
                // the "saveApplicationDatabase" method saves the new line and make that the when the method
                // "loadApplicationDatabase" is executed make the load from the last key path the new line characters
                //      -biologyiswell, 13 May 2018
                sb.deleteCharAt(sb.length() - 1);
            }

            return sb.toString();
        } catch (IOException e) {
            errorf("An internal error occured when read a file content (with exception). (Report to an administrator)\n");
            e.printStackTrace();
            exit(0);
        }

        return null;
    }

    /**
     * Run Os Command, method,
     * This method run an operational system command
     *
     * @param directory the directory from the command
     * @param commands the commands that will be executed by the method
     * @since 0.1
     */
    private void runOsCommand(final File directory, final String... commands) {
        // @Note Check if the commands is null
        if (commands == null) {
            errorf("An internal error occured when run operational system command \"%\". (Report to an administrator)\n", "commands is null");
            exit(0);

            throw new NullPointerException("an internal error occured when run operational system command (commands is null)");
        }

        try {
            final ProcessBuilder processBuilder = new ProcessBuilder();

            // @Note Chcek if the directory is different from null and if the directory exists, to set the the
            // directory to the process builder from operational system command
            if (directory != null && directory.exists()) {
                processBuilder.directory(directory);
            }

            processBuilder.command(commands);
            processBuilder.inheritIO().start().waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Exit, method,
     * This method wait a milliseconds until to exit the program, this represents a safe method that is used to exit
     * the program safely, do not use "System.exit(-1)"
     *
     * @param millis the milliseconds until exit program
     * @param ignoreSaveApplicationDatabase if true the save application in database will be ignored, otherwise if false
     *                                      the save application in database is completed
     * @since 1.1
     */
    private void exit(long millis, boolean ignoreSaveApplicationDatabase) {
        try {
            Thread.sleep(millis);

            // @Note This condition represents if the save of database is to be ignored
            if (!ignoreSaveApplicationDatabase) {
                // @Note Save the application database
                this.saveApplicationDatabase();
            }

            // @Note Close the application
            System.exit(-1);
        } catch (Exception e) {
            errorf("An internal error occured when wait \"%\" milliseconds until close the application. (Report to an administrator)\n", millis);
            e.printStackTrace();
        }
    }

    /**
     * Exit, method,
     * This method wait a milliseconds until to exit the program, this represents a safe method that is used to exit
     * the program safely, do not use "System.exit(-1)", this method makes the save application in database
     *
     * @see {@link #exit(long, boolean)}
     * @param millis the milliseconds until exit program
     * @since 1.1
     */
    private void exit(long millis) {
        this.exit(millis, false);
    }
}
