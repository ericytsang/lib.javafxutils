package com.github.ericytsang.lib.javafxutils

import javafx.scene.control.TextField

/**
 * Created by surpl on 8/14/2016.
 */
abstract class ValidatableTextField:TextField()
{
    companion object
    {
        fun makeIntegerTextField():TextField
        {
            return object:ValidatableTextField()
            {
                override fun validate(text:String):Boolean
                {
                    return text.matches(Regex("-?[0-9]*"))
                }
            }
        }

        fun makeDecimalTextField():TextField
        {
            return object:ValidatableTextField()
            {
                override fun validate(text:String):Boolean
                {
                    return text.matches(Regex("-?[0-9]*(.[0-9]*)?"))
                }
            }
        }
    }

    override fun replaceText(start:Int,end:Int,text:String)
    {
        if (validate(text))
        {
            super.replaceText(start, end, text)
        }
    }

    override fun replaceSelection(replacement:String)
    {
        if (validate(text))
        {
            super.replaceSelection(text)
        }
    }

    protected abstract fun validate(text:String):Boolean
}
