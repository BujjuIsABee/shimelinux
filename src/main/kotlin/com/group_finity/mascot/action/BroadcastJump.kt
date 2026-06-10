/*
 * Copyright (c) 2026, Bujju
 * All rights reserved.
 * License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE
 * Original License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE-ORIGINAL
 */

package com.group_finity.mascot.action

import com.group_finity.mascot.animation.Animation
import com.group_finity.mascot.script.VariableMap
import java.util.ResourceBundle

@Deprecated("Deprecated in Shimeji-ee")
class BroadcastJump(
    schema: ResourceBundle,
    animations: List<Animation>,
    context: VariableMap
) : Jump(schema, animations, context)
