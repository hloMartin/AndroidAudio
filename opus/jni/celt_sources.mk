CELT_SOURCES = \
opus/celt/bands.c \
opus/celt/celt.c \
opus/celt/celt_encoder.c \
opus/celt/celt_decoder.c \
opus/celt/cwrs.c \
opus/celt/entcode.c \
opus/celt/entdec.c \
opus/celt/entenc.c \
opus/celt/kiss_fft.c \
opus/celt/laplace.c \
opus/celt/mathops.c \
opus/celt/mdct.c \
opus/celt/modes.c \
opus/celt/pitch.c \
opus/celt/celt_lpc.c \
opus/celt/quant_bands.c \
opus/celt/rate.c \
opus/celt/vq.c

CELT_SOURCES_SSE = \
opus/celt/x86/x86cpu.c \
opus/celt/x86/x86_celt_map.c \
opus/celt/x86/pitch_sse.c

CELT_SOURCES_SSE2 = \
opus/celt/x86/pitch_sse2.c \
opus/celt/x86/vq_sse2.c

CELT_SOURCES_SSE4_1 = \
opus/celt/x86/celt_lpc_sse4_1.c \
opus/celt/x86/pitch_sse4_1.c

CELT_SOURCES_ARM = \
opus/celt/arm/armcpu.c \
opus/celt/arm/arm_celt_map.c

CELT_SOURCES_ARM_ASM = \
opus/celt/arm/celt_pitch_xcorr_arm.s

CELT_AM_SOURCES_ARM_ASM = \
opus/celt/arm/armopts.s.in

CELT_SOURCES_ARM_NEON_INTR = \
opus/celt/arm/celt_neon_intr.c \
opus/celt/arm/pitch_neon_intr.c

CELT_SOURCES_ARM_NE10 = \
opus/celt/arm/celt_fft_ne10.c \
opus/celt/arm/celt_mdct_ne10.c
