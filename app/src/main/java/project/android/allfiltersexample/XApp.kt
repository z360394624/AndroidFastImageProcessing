package project.android.allfiltersexample

import android.app.Application
import com.qw.soul.permission.SoulPermission
import project.android.allfiltersexample.utils.FileUtil
import java.io.File

/**
 * User: wujinsheng1@yy.com
 * Date: 2020/5/14 17:24
 * ModifyTime: 17:24
 * Description:
 */
class XApp : Application() {
    override fun onCreate() {
        super.onCreate()
        SoulPermission.init(this)

        FileUtil.copyAssets(
            this,
            "filterData.zip",
            File(FileUtil.getCacheDirectory(this), "filterData.zip")
        )
        FileUtil.unzip(
            File(FileUtil.getCacheDirectory(this), "filterData.zip").absolutePath,
            FileUtil.getCacheDirectory(this).absolutePath,
            false
        )

    }
}