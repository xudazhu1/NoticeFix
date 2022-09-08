package com.xeasy.noticefix.utils;

import android.content.Context;
import android.widget.Toast;

import com.topjohnwu.superuser.Shell;
import com.xeasy.noticefix.R;

import java.util.List;

public class CommandUtil {

    /**
     * 执行命令
     *
     * @param cmd  命令
     * @param isSu 是否使用 Root 权限执行 - 默认：是
     * @return [String] 执行结果
     */
    public static String execShell(String cmd, Boolean isSu) {
        Shell.Job job;
        if ((isSu)) {
            job = Shell.su(cmd);
        } else {
            job = Shell.sh(cmd);
        }
        Shell.Result exec = job.exec();
        List<String> out = exec.getOut();
        if ( ! out.isEmpty() ) {
            return out.get(0);
        }
        return "";
    }

    public static void restartSystemUI(Context context) {
        String pid = execShell("pgrep systemui", true);
        if (  ! pid.isEmpty() ) {
            execShell("kill -9 " + pid, true);
        } else {
            // No root privileges!!
            Toast.makeText(context, context.getString(R.string.no_root), Toast.LENGTH_SHORT).show();
        }

    }

}