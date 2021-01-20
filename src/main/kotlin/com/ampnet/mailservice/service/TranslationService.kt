package com.ampnet.mailservice.service

import com.github.mustachejava.Mustache

interface TranslationService {
    fun getTitleTranslations(titleKey: String): Map<String, String>
    fun getTemplateTranslations(templateName: String): Map<String, Mustache>
}
