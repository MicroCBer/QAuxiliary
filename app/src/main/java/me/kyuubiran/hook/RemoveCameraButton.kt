/*
 * QAuxiliary - An Xposed module for QQ/TIM
 * Copyright (C) 2019-2022 qwq233@qwq2333.top
 * https://github.com/cinit/QAuxiliary
 *
 * This software is non-free but opensource software: you can redistribute it
 * and/or modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either
 * version 3 of the License, or any later version and our eula as published
 * by QAuxiliary contributors.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * and eula along with this software.  If not, see
 * <https://www.gnu.org/licenses/>
 * <https://github.com/cinit/QAuxiliary/blob/master/LICENSE.md>.
 */

package me.kyuubiran.hook

import android.view.View
import com.github.kyuubiran.ezxhelper.utils.findMethod
import com.github.kyuubiran.ezxhelper.utils.hookBefore
import io.github.qauxv.base.annotation.FunctionHookEntry
import io.github.qauxv.base.annotation.UiItemAgentEntry
import io.github.qauxv.dsl.FunctionEntryRouter.Locations.Simplify
import io.github.qauxv.hook.CommonSwitchFunctionHook
import io.github.qauxv.util.Initiator
import xyz.nextalone.util.throwOrTrue

@FunctionHookEntry
@UiItemAgentEntry
object RemoveCameraButton : CommonSwitchFunctionHook("kr_disable_camera_button") {

    override val name: String = "屏蔽聊天界面相机图标"

    override fun initOnce() = throwOrTrue {
        findMethod(Initiator._ConversationTitleBtnCtrl()) { name == "a" && returnType == Void.TYPE && parameterTypes.contentEquals(arrayOf(View::class.java)) }.hookBefore {
            if (!isEnabled) return@hookBefore; it.result = null
        }
        findMethod(Initiator._ConversationTitleBtnCtrl()) { name == "a" && returnType == Void.TYPE && parameterTypes.isEmpty() }.hookBefore {
            if (!isEnabled) return@hookBefore; it.result = null
        }
    }

    override val uiItemLocation: Array<String> = Simplify.CHAT_GROUP_OTHER
}
