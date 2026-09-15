<template>
  <div ref="layer" class="watermark-layer"></div>
</template>

<script>
/**
 * 全局页面水印：平铺显示 登录用户姓名(xm) / 身份证号(sfzh) / 访问页面时间(年月日时分秒)。
 * 采用离屏 canvas 生成平铺背景图，颜色较浅、半透明，pointer-events:none，
 * 覆盖在所有页面之上但不遮挡内容、不影响任何点击/选择操作。
 * 时间每秒刷新一次，使水印始终反映当前访问时刻，便于截图溯源。
 * 时间以服务器时间为准：父组件传入 timeOffset（服务器时间 - 客户端时间 的偏移量），
 * 本地时钟不准时水印时间依然准确；偏移量为 0（默认）时退化为客户端本地时间。
 */
export default {
  name: 'Watermark',
  props: {
    /** 登录用户姓名（syngbs.manex.xm） */
    userName: {
      type: String,
      default: ''
    },
    /** 登录用户身份证号（syngbs.manex.sfzh） */
    sfzh: {
      type: String,
      default: ''
    },
    /** 服务器时间与客户端时间的偏移量（毫秒），水印显示 本地时间 + 偏移 */
    timeOffset: {
      type: Number,
      default: 0
    }
  },
  mounted() {
    this.render()
    this.timer = setInterval(this.render, 1000)
  },
  beforeDestroy() {
    if (this.timer) {
      clearInterval(this.timer)
      this.timer = null
    }
  },
  watch: {
    userName() {
      this.render()
    },
    sfzh() {
      this.render()
    },
    timeOffset() {
      this.render()
    }
  },
  methods: {
    pad(n) {
      return n < 10 ? '0' + n : '' + n
    },
    /** 当前时间（按服务器时间校准），格式：年月日时分秒（yyyy-MM-dd HH:mm:ss） */
    nowText() {
      const d = new Date(Date.now() + (this.timeOffset || 0))
      return d.getFullYear() + '-' + this.pad(d.getMonth() + 1) + '-' + this.pad(d.getDate()) +
        ' ' + this.pad(d.getHours()) + ':' + this.pad(d.getMinutes()) + ':' + this.pad(d.getSeconds())
    },
    /** 水印文本行：姓名、身份证号、访问页面时间 */
    watermarkLines() {
      const lines = []
      if (this.userName) {
        lines.push('姓名：' + this.userName)
      }
      if (this.sfzh) {
        lines.push('身份证号：' + this.sfzh)
      }
      lines.push('访问时间：' + this.nowText())
      return lines
    },
    render() {
      const layer = this.$refs.layer
      if (!layer) {
        return
      }
      const lines = this.watermarkLines()
      const fontSize = 18
      const lineHeight = 28
      const font = fontSize + 'px "Microsoft YaHei", "PingFang SC", sans-serif'
      const gap = 40
      const dpr = window.devicePixelRatio || 1

      const canvas = document.createElement('canvas')
      const ctx = canvas.getContext('2d')
      ctx.font = font
      let maxWidth = 0
      lines.forEach(line => {
        maxWidth = Math.max(maxWidth, ctx.measureText(line).width)
      })

      // 旋转角 -22°，按旋转后的外接矩形计算平铺块尺寸，避免文字被裁切
      const angle = -22 * Math.PI / 180
      const cos = Math.cos(Math.abs(angle))
      const sin = Math.sin(Math.abs(angle))
      const textHeight = lines.length * lineHeight
      const tileWidth = Math.ceil(maxWidth * cos + textHeight * sin) + gap
      const tileHeight = Math.ceil(maxWidth * sin + textHeight * cos) + gap

      canvas.width = tileWidth * dpr
      canvas.height = tileHeight * dpr
      ctx.scale(dpr, dpr)
      ctx.font = font
      // 半透明深灰（约 15% 不透明度），白底页面上隐约可见但基本不影响阅读
      ctx.fillStyle = 'rgba(0, 0, 0, 0.15)'
      ctx.textAlign = 'center'
      ctx.textBaseline = 'middle'
      ctx.translate(tileWidth / 2, tileHeight / 2)
      ctx.rotate(angle)
      const startY = -(lines.length - 1) * lineHeight / 2
      lines.forEach((line, i) => {
        ctx.fillText(line, 0, startY + i * lineHeight)
      })

      layer.style.backgroundImage = 'url(' + canvas.toDataURL('image/png') + ')'
      layer.style.backgroundSize = tileWidth + 'px ' + tileHeight + 'px'
    }
  }
}
</script>

<style scoped>
.watermark-layer {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 9999;
  pointer-events: none;
  background-repeat: repeat;
  user-select: none;
}
</style>
