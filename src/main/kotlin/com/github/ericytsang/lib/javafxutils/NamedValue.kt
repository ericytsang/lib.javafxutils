package com.github.ericytsang.lib.javafxutils

import javafx.scene.control.ComboBox

/**
 * meant to be used in [ComboBox] objects.
 *
 * Created by surpl on 8/21/2016.
 */
class NamedValue<out Value>(val name:String,val value:Value)
{
    override fun toString():String = name
}
