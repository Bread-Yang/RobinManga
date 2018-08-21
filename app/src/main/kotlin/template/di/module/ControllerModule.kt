package template.di.module

import com.bluelinelabs.conductor.Controller
import dagger.Module
import template.di.scopes.ControllerContext

@Module
@ControllerContext
class ControllerModule(private val controller: Controller)