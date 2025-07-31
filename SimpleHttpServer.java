
// SimpleHttpServer.java
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class SimpleHttpServer {
    private static final int PORT = 9090;
    private static final String TEMP_DIR = "temp";

    public static void main(String[] args) throws Exception {
        Files.createDirectories(Paths.get(TEMP_DIR));
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Server running at http://localhost:" + PORT);

        while (true) {
            Socket client = serverSocket.accept();
            new Thread(() -> handleClient(client)).start();
        }
    }

    private static void handleClient(Socket client) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                OutputStream out = client.getOutputStream()) {

            String line;
            String method = "", path = "";
            boolean isPost = false;

            while ((line = in.readLine()) != null && !line.isEmpty()) {
                if (line.startsWith("GET") || line.startsWith("POST")) {
                    String[] parts = line.split(" ");
                    method = parts[0];
                    path = parts[1];
                    isPost = method.equals("POST");
                }
            }

            if (!isPost) {
                sendMainPage(out, "", "", "");
            } else {
                StringBuilder bodyBuilder = new StringBuilder();
                while (in.ready()) {
                    bodyBuilder.append((char) in.read());
                }
                String data = bodyBuilder.toString().trim();

                String[] parts = data.split("&");
                String lang = "", code = "";
                for (String part : parts) {
                    if (part.startsWith("language=")) {
                        lang = URLDecoder.decode(part.split("=")[1], "UTF-8");
                    } else if (part.startsWith("code=")) {
                        code = URLDecoder.decode(part.split("=")[1], "UTF-8");
                    }
                }

                String result = runCode(code, lang);
                sendMainPage(out, lang, code, result);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sendMainPage(OutputStream out, String selectedLang, String code, String output)
            throws IOException {
        String html = String.format(
                """
                        <html>
                        <head>
                            <title>DevHub - Code & Learn</title>
                            <style>
                                body {
                                    margin: 0;
                                    font-family: 'Segoe UI', sans-serif;
                                    background: linear-gradient(135deg, #1a1a2e, #16213e);
                                    color: white;
                                }
                                .container {
                                    display: flex;
                                    gap: 20px;
                                    padding: 30px;
                                }
                                .form-section, .learn-section {
                                    flex: 1;
                                    padding: 20px;
                                    background-color: rgba(255, 255, 255, 0.05);
                                    border-radius: 20px;
                                }
                                h2 {
                                    text-align: center;
                                    color: #00fff7;
                                }
                                select, textarea, input[type=submit] {
                                    width: 100%%;
                                    padding: 12px;
                                    border-radius: 10px;
                                    border: none;
                                    margin-bottom: 15px;
                                    font-size: 16px;
                                    background-color: #0f3460;
                                    color: white;
                                }
                                textarea {
                                    height: 250px;
                                    font-family: monospace;
                                }
                                input[type=submit] {
                                    background-color: #00ff88;
                                    color: black;
                                    font-weight: bold;
                                    cursor: pointer;
                                }
                                input[type=submit]:hover {
                                    background-color: #14f7a8;
                                }
                                pre {
                                    white-space: pre-wrap;
                                    word-wrap: break-word;
                                    background: #111;
                                    padding: 15px;
                                    border-radius: 10px;
                                    font-family: monospace;
                                    color: #00ffcc;
                                }
                                .card {
                                    background-color: rgba(255, 255, 255, 0.05);
                                    margin-bottom: 15px;
                                    padding: 15px;
                                    border-radius: 10px;
                                }
                                .card h3 {
                                    margin-top: 0;
                                    color: #ffee58;
                                }
                                .card a {
                                    display: inline-block;
                                    margin-right: 10px;
                                    color: #00e6ff;
                                    text-decoration: none;
                                }
                                .card a:hover {
                                    color: #00ff88;
                                }
                            </style>
                        </head>
                        <body>
                            <div class='container'>
                                <div class='form-section'>
                                    <h2> Start Coding</h2>
                                    <form method='post'>
                                        <label>Choose Language:</label>
                                        <select name='language'>
                                            <option value='java' %s>Java</option>
                                            <option value='c' %s>C</option>
                                            <option value='cpp' %s>C++</option>
                                            <option value='python' %s>Python</option>
                                            <option value='php' %s>PHP</option>
                                            <option value='ruby' %s>Ruby</option>
                                            <option value='perl' %s>Perl</option>
                                        </select>
                                        <label>Write Your Code:</label>
                                        <textarea name='code'>%s</textarea>
                                        <input type='submit' value=' Run Code'>
                                    </form>
                                    <h2> Output</h2>
                                    <pre>%s</pre>
                                </div>
                                <div class='learn-section'>
                                    <h2> Learn Languages</h2>
                                    <div class='card'><h3>Java</h3><a href='https://dev.java/learn/' target='_blank'>Official</a><a href='https://youtube.com/results?search_query=java+programming+tutorial' target='_blank'>YouTube</a></div>
                                    <div class='card'><h3>C</h3><a href='https://gcc.gnu.org/' target='_blank'>Official</a><a href='https://youtube.com/results?search_query=c+programming+tutorial' target='_blank'>YouTube</a></div>
                                    <div class='card'><h3>C++</h3><a href='https://isocpp.org/' target='_blank'>Official</a><a href='https://youtube.com/results?search_query=c++programming+tutorial' target='_blank'>YouTube</a></div>
                                    <div class='card'><h3>Python</h3><a href='https://python.org/doc/' target='_blank'>Official</a><a href='https://youtube.com/results?search_query=python+programming+tutorial' target='_blank'>YouTube</a></div>
                                    <div class='card'><h3>PHP</h3><a href='https://www.php.net/manual/en/' target='_blank'>Official</a><a href='https://youtube.com/results?search_query=php+programming+tutorial' target='_blank'>YouTube</a></div>
                                    <div class='card'><h3>Ruby</h3><a href='https://www.ruby-lang.org/en/documentation/' target='_blank'>Official</a><a href='https://youtube.com/results?search_query=ruby+programming+tutorial' target='_blank'>YouTube</a></div>
                                    <div class='card'><h3>Perl</h3><a href='https://perldoc.perl.org/' target='_blank'>Official</a><a href='https://youtube.com/results?search_query=perl+programming+tutorial' target='_blank'>YouTube</a></div>
                                </div>
                            </div>
                        </body>
                        </html>
                        """,
                selectedLang.equals("java") ? "selected" : "",
                selectedLang.equals("c") ? "selected" : "",
                selectedLang.equals("cpp") ? "selected" : "",
                selectedLang.equals("python") ? "selected" : "",
                selectedLang.equals("php") ? "selected" : "",
                selectedLang.equals("ruby") ? "selected" : "",
                selectedLang.equals("perl") ? "selected" : "",
                code == null ? "" : code.replace("<", "&lt;").replace(">", "&gt;"),
                output == null || output.isEmpty() ? " Output will be shown here." : output);

        sendResponse(out, html);
    }

    private static void sendResponse(OutputStream out, String body) throws IOException {
        byte[] response = body.getBytes(StandardCharsets.UTF_8);
        out.write(("HTTP/1.1 200 OK\r\nContent-Type: text/html\r\nContent-Length: " + response.length + "\r\n\r\n")
                .getBytes());
        out.write(response);
        out.flush();
    }

    private static String runCode(String code, String language) {
        try {
            String fileName, compileCmd = null, runCmd;
            String executable = TEMP_DIR + "/main";

            switch (language) {
                case "java":
                    fileName = "Main.java";
                    // Replace any class name with "Main"
                    code = code.replaceAll("class\\s+\\w+", "class Main");
                    compileCmd = "javac " + TEMP_DIR + "/" + fileName;
                    runCmd = "java -cp " + TEMP_DIR + " Main";
                    break;
                case "cpp":
                    fileName = "main.cpp";
                    compileCmd = "g++ " + TEMP_DIR + "/" + fileName + " -o " + executable;
                    runCmd = executable;
                    break;
                case "c":
                    fileName = "main.c";
                    compileCmd = "gcc " + TEMP_DIR + "/" + fileName + " -o " + executable;
                    runCmd = executable;
                    break;
                case "python":
                    fileName = "main.py";
                    runCmd = "python " + TEMP_DIR + "/" + fileName;
                    break;
                case "php":
                    fileName = "main.php";
                    runCmd = "php " + TEMP_DIR + "/" + fileName;
                    break;
                case "ruby":
                    fileName = "main.rb";
                    runCmd = "ruby " + TEMP_DIR + "/" + fileName;
                    break;
                case "perl":
                    fileName = "main.pl";
                    runCmd = "perl " + TEMP_DIR + "/" + fileName;
                    break;
                default:
                    return " Unsupported language selected.";
            }

            // Save the code to file
            Files.writeString(Paths.get(TEMP_DIR, fileName), code, StandardCharsets.UTF_8);

            // Compile (if needed)
            if (compileCmd != null) {
                Process compile = Runtime.getRuntime().exec(compileCmd);
                compile.waitFor();
                if (compile.exitValue() != 0) {
                    return new String(compile.getErrorStream().readAllBytes());
                }
            }

            // Run the code
            Process run = Runtime.getRuntime().exec(runCmd);
            run.waitFor();

            String output = new String(run.getInputStream().readAllBytes());
            String error = new String(run.getErrorStream().readAllBytes());

            return output + (error.isEmpty() ? "" : "\nError:\n" + error);
        } catch (Exception e) {
            return " Execution Error: " + e.getMessage();
        }
    }

}