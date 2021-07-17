package com.kdaydin.photofilter.ui.filter

import com.kdaydin.photofilter.ui.base.VMState

interface FilterVMState : VMState {
    class SavePhoto : FilterVMState
    class CloseApp : FilterVMState
}