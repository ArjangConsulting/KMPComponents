package io.github.maniramezan.kmpcomponents

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ProviderConfigurationTest {
    private val complete = ProviderConfigurationState(
        providerName = "Example",
        baseUrl = "https://api.example.com/v1",
        model = "example-large",
        apiKey = "sk-secret-value",
    )

    @Test
    fun `toString never exposes the api key`() {
        val rendered = complete.toString()
        assertFalse("sk-secret-value" in rendered, rendered)
        assertTrue("<redacted>" in rendered, rendered)
        assertTrue("providerName=Example" in rendered, rendered)
    }

    @Test
    fun `toString shows an empty api key as empty`() {
        assertFalse("<redacted>" in ProviderConfigurationState().toString())
    }

    @Test
    fun `complete state is valid`() {
        assertTrue(complete.isValid())
        assertTrue(complete.validate().isEmpty())
    }

    @Test
    fun `blank fields are required`() {
        val errors = ProviderConfigurationState(providerName = "  ").validate()
        assertEquals(
            ProviderConfigurationField.entries.associateWith { ProviderConfigurationError.REQUIRED },
            errors,
        )
    }

    @Test
    fun `api key is optional when not required`() {
        val state = complete.copy(apiKey = "")
        assertTrue(state.isValid(apiKeyRequired = false))
        assertEquals(
            mapOf(ProviderConfigurationField.API_KEY to ProviderConfigurationError.REQUIRED),
            state.validate(apiKeyRequired = true),
        )
    }

    @Test
    fun `base url must be absolute http or https`() {
        listOf(
            "api.example.com",
            "ftp://example.com",
            "https://",
            "https://exa mple.com",
            "https://:oops",
            "https://example.com:99999",
            "https://example.com:",
            "https://example.com:+80",
            "https://example..com",
            "https://user@example.com",
            "http://[:::]",
            "http://[::ffff:999.1.1.1]",
        ).forEach { url ->
            assertEquals(
                ProviderConfigurationError.INVALID_URL,
                complete.copy(baseUrl = url).validate()[ProviderConfigurationField.BASE_URL],
                url,
            )
        }
        listOf(
            "http://localhost:11434",
            "HTTPS://Example.com",
            " https://example.com/v1?x=1 ",
            "http://[::1]:11434",
        ).forEach { url ->
            assertTrue(complete.copy(baseUrl = url).isValid(), url)
        }
    }

    @Test
    fun `labels map every error to a message`() {
        val labels = ProviderConfigurationLabels(requiredError = "req", invalidUrlError = "url")
        assertEquals("req", labels.messageFor(ProviderConfigurationError.REQUIRED))
        assertEquals("url", labels.messageFor(ProviderConfigurationError.INVALID_URL))
    }
}
