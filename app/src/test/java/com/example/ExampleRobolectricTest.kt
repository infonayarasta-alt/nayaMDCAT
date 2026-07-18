package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ui.MainViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("NayaRasta", appName)
  }

  @Test
  fun `test MainViewModel initialization`() {
    val application = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = MainViewModel(application)
    assertNotNull(viewModel)
  }

  @Test
  fun `test authenticateUser flow`() {
    val application = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = MainViewModel(application)
    var callbackCalled = false
    viewModel.authenticateUser("user1", "user123") { success ->
        callbackCalled = true
        println("Authentication callback: $success")
    }
    org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
  }
}
