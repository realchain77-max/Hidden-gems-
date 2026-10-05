package com.example

import com.example.util.AntiFraudManager
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun antiFraud_verificationHash_isGenerated() {
    val lat = -1.2884
    val lng = 36.7820
    val timestamp = 1728135000000L
    val hash = AntiFraudManager.generateVerificationHash(lat, lng, timestamp)
    assertNotNull(hash)
    assertEquals(16, hash.length)
  }

  @Test
  fun antiFraud_distanceCalculation_isAccurate() {
    // Distance between same point is 0
    val distance = AntiFraudManager.calculateDistanceMeters(-1.2884, 36.7820, -1.2884, 36.7820)
    assertEquals(0.0, distance, 0.01)
  }
}
