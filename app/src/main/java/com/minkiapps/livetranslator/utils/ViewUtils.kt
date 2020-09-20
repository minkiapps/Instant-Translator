package com.minkiapps.livetranslator.utils

import android.animation.Animator
import android.view.View

fun View.setRotateView180OnClick(viewToRotate : View = this, onclick: (v: View) -> Unit) {
    setOnClickListener {
        it.isEnabled = false
        viewToRotate.animate().rotationBy(180f).setListener(AnimatorEndListener {
            it.isEnabled = true
        })
        onclick.invoke(it)
    }
}

class AnimatorEndListener(private val animEnd: () -> Unit) : Animator.AnimatorListener {
    override fun onAnimationRepeat(animation: Animator?) {

    }

    override fun onAnimationEnd(animation: Animator?) {
        animEnd.invoke()
    }

    override fun onAnimationCancel(animation: Animator?) {
    }

    override fun onAnimationStart(animation: Animator?) {
    }
}