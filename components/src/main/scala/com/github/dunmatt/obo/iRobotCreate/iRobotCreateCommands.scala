package com.github.dunmatt.obo.iRobotCreate

object Commands {
  val STRAIGHT = 0x7FFF
  val START = Array(128.toByte)
  val PASSIVE = START
  val CONTROL = Array(130.toByte)
  val SAFE = Array(131.toByte)
  val FULL = Array(132.toByte)
  val SPOT = Array(134.toByte)
  val COVER = Array(135.toByte)
  val DEMO = 136.toByte  // 136 demoNumber
  val DRIVE = 137.toByte  // 137 velHigh velLow radHigh radLow
  val LOW_SIDE_DRIVERS = 138.toByte  // 138 bits
  val LEDS = 139.toByte  // 139 ledBits powerColor powerIntensity
  val SONG = 140.toByte  // 140 songNumber songLength note1 note1dur note2...
  val PLAY = 141.toByte  // 141 songNumber
  val SENSORS = 142.toByte  // 142 packetId
  val COVER_AND_DOCK = Array(143.toByte)
  val PWM_LD = 144.toByte // 144 ld2DutyCycle ld1DutyCycle ld0DutyCycle
  val DRIVE_DIRECT = 145.toByte // 145 rightHigh rightLow leftHigh leftLow
  val STREAM = 148.toByte // 148 numIds id id id id...
  val QUERY_LIST = 149.toByte // 149 numIds id id id id...
  val PAUSE_STREAM = Array(150.toByte, 0.toByte)
  val RESUME_STREAM = Array(150.toByte, 1.toByte)
  // NOTE: 151 assumes special hardware connected to LD1, we don't currently have that hardware.  
  // static final byte SEND_IR = (byte)151;  // 151 message 
  val SCRIPT = 152.toByte // 152 scriptLength opcode1 data1 opcode2 data2 ...
  val PLAY_SCRIPT = Array(153.toByte)
  val SHOW_SCRIPT = Array(154.toByte)
  val WAIT_TIME = 155.toByte  // 151 duration
  val WAIT_DISTANCE = 156.toByte // 156 distanceHigh distanceLow
  val WAIT_ANGLE = 157.toByte // 157 angleHigh angleLow
  val WAIT_EVENT = 158.toByte  // 151 eventId

  val STOP = Array(FULL(0), DRIVE_DIRECT, 0, 0, 0, 0, LOW_SIDE_DRIVERS, 0, SAFE(0)).map(_.toByte)
  val SPIN_LEFT = Array(DRIVE, 0, 0, 0xFF, 0xFF).map(_.toByte)
  val SPIN_RIGHT = Array(DRIVE, 0, 0, 0, 1).map(_.toByte)

  val LOAD_STATUS_SONGS = Array(SONG, 0, 6, 79, 8, 84, 8, 88, 8, 91, 12, 88, 4, 91, 32, SONG, 1, 3, 69, 24, 61, 24, 52, 64).map(_.toByte)

  val SING_READY = Array(PLAY, 0).map(_.toByte)
  val SING_DRAMATIC = Array(PLAY, 1).map(_.toByte)
}
