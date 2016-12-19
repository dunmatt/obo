package com.github.dunmatt.obo.android.core

import android.content.Context
import com.github.dunmatt.obo.core.Component

trait AndroidComponent extends Component {
  var context: Context = null  // this will be populated by the component runner before the component is started
}
