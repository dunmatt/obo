package com.github.dunmatt.obo.iRobotCreate

object iRobotCreateState {
  sealed trait ChargingState
  sealed trait RobotMode
  
  object NOT_CHARGING   extends ChargingState
  object RECONDITIONING extends ChargingState
  object FULL           extends ChargingState
  object TRICKLE        extends ChargingState
  object WAITING        extends ChargingState
  object CHARGING_FAULT extends ChargingState
  object UNKNOWN        extends ChargingState with RobotMode

  object OFF          extends RobotMode
  object PASSIVE      extends RobotMode
  object SAFE         extends RobotMode
  object FULL_CONTROL extends RobotMode
}
