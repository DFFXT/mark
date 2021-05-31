import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;

/**
 * git 服务端 hook
 * 使用方式：放在服务端对应仓库git跟目录下
 * update 钩子调用 java Bridge [--config configPath]
 * configPath 指配置文件地址，格式暂定
 */
public class Bridge {

    static String ktlintPath = "./hooks/check/ktlint.jar";
    static String tmpDir = "push_file_tmp_dir/";

    public static void main(String[] args) {
        Runtime runtime = Runtime.getRuntime();
        try {
            String oldVersion = args[0];
            String newVersion = args[1];
            Process process = runtime.exec("git rev-list "+oldVersion+".."+newVersion);
            String[] commits = getResult(process).split("\n");
            for(String head:commits){
                deal(head);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static void deal(String head) throws IOException {
        Process process = Runtime.getRuntime().exec("git show "+head+" --pretty=format:'%t'");
        String p = getResult(process);
        String[] lines = p.split("\n");
        ArrayList<String> fileHash = new ArrayList<>();
        for (String line : lines) {//获取文件对应的hash
            if (line.startsWith("index")) {
                log(line);
                fileHash.add(line.substring(15, 22));
                log(fileHash.get(fileHash.size() - 1));
            }
        }

        process = Runtime.getRuntime().exec("git log "+head+" --name-only --pretty=format:''");
        String res = getResult(process).trim();
        String[] files = res.split("\n");
        for (String line : files) {
            log("line:" + line);
        }
        StringBuilder ktFileList = new StringBuilder();
        for (int i = 0; i < fileHash.size(); i++) {
            String path = files[i + 1];
            write(files[i + 1], fileHash.get(i));
            if (path.endsWith(".java")) {

            } else if (path.endsWith(".kt")) {
                if (ktFileList.length() == 0) {
                    ktFileList.append(tmpDir).append(path);
                } else {
                    ktFileList.append(" ");
                    ktFileList.append(tmpDir);
                    ktFileList.append(path);
                }
            }


        }
        String cmd = "java -jar " + ktlintPath + " ktlint " + ktFileList.toString();
        log(cmd);
        process = Runtime.getRuntime().exec(cmd);
        String result = getResult(process);
        log(result);
    }

    private static String getResult(Process process) {
        try (InputStream ips = process.getInputStream(); ByteArrayOutputStream bops = new ByteArrayOutputStream()) {
            byte[] bytes = new byte[1024];
            int offset;
            while ((offset = ips.read(bytes)) != -1) {
                bops.write(bytes, 0, offset);
            }
            return new String(bops.toByteArray(), 0, bops.size(), Charset.forName("UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private static void write(String path, String hash) {
        File file = new File(tmpDir + path);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            if (!parent.mkdirs()) {
                return;
            }
        }
        log(file.getAbsolutePath());
        try (FileOutputStream fos = new FileOutputStream(file)) {
            Process process = Runtime.getRuntime().exec("git cat-file -p " + hash);
            fos.write(getResult(process).getBytes());
            fos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void log(String log) {
        System.out.println(log);
    }

    /**
     * 删除缓存文件夹
     * @throws IOException io
     */
    public static void deleteTmpDir() throws IOException {
        Runtime.getRuntime().exec("rm -rf "+tmpDir);
    }
}
