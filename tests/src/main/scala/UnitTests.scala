package com.github.slashmili.Zendroid.tests

import junit.framework.Assert._
import _root_.android.test.AndroidTestCase

class UnitTests extends AndroidTestCase {
  def testPackageIsCorrect {
    assertEquals("com.github.slashmili.Zendroid", getContext.getPackageName)
  }
}