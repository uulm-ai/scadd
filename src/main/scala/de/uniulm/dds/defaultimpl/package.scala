package de.uniulm.dds

import de.uniulm.dds.base.Context.ContextFactory

/**
  * Contains the method for creating contexts
  * Created by Felix on 02.12.13.
  */
package object defaultimpl {
  implicit def voFactory[V, T]: ContextFactory[V, T, Parameters] = (p: Parameters[V, T]) => new DefaultContext[V, T](p)
}
