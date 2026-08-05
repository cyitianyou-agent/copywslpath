package com.cyitianyou.copywslpath

/**
 * Concrete Project View actions. Each class contains only its mode and the
 * corresponding settings selector; all operational behavior lives in the
 * shared base action.
 */
class CopyPathAction : BaseCopyPathAction(CopyMode.RAW, { showCopyPathMenu })

class CopyRealPathAction : BaseCopyPathAction(CopyMode.REAL, { showCopyRealPathMenu })

class CopyWslPathAction : BaseCopyPathAction(CopyMode.WSL, { showCopyWslPathMenu })

class CopyRealWslPathAction : BaseCopyPathAction(CopyMode.REAL_WSL, { showCopyRealWslPathMenu })
