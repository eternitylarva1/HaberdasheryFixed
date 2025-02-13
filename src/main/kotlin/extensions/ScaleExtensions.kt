package com.evacipated.cardcrawl.mod.haberdashery.extensions

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Vector2
import com.megacrit.cardcrawl.core.Settings

fun Int.scale(): Float =
    this * Settings.scale

fun Float.scale(): Float =
    this * Settings.scale

fun Int.renderScale(): Float =
    this * Settings.renderScale

fun Float.renderScale(): Float =
    this * Settings.renderScale

fun Int.timeScale(): Float =
    this * Gdx.graphics.deltaTime

fun Float.timeScale(): Float =
    this * Gdx.graphics.deltaTime

fun Float.flipY(): Float =
    Settings.HEIGHT - this

fun Vector2.flipY(): Vector2 =
    apply { y = y.flipY() }
