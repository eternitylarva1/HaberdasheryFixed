package haberdashery.util

import com.megacrit.cardcrawl.core.CardCrawlGame

class L10nStrings(
    private val text: Map<String, String>?
) {
    constructor(id: String) : this(CardCrawlGame.languagePack.getUIString(id)?.TEXT_DICT)

    operator fun get(key: String): String {
        return text?.get(key) ?: "[MISSING:$key]"
    }
}
