ACADO_PATH = /media/data/kodning/MPC/ACADOtoolkit-1.0.2613beta
BUILD_PATH = bin
SOURCE_PATH = src
EXE_PATH = $(BUILD_PATH)

include ${ACADO_PATH}/include/acado/include.mk
include ${ACADO_PATH}/include/acado/include_aux.mk

ACADO_LIB_DIR = $(ACADO_PATH)/libs
ACADO_INCLUDE_DIR =  $(ACADO_PATH)/include
LIBS = -L$(ACADO_LIB_DIR) ${filter-out -L%, $(TOOLKIT_LIBS)}
##
##	flags
##

IFLAGS	= -I. \
			${HEADER_PATHS} \
			-I$(ACADO_INCLUDE_DIR)

CPPFLAGS  =	${IFLAGS} ${CPP_GLOBAL_FLAGS}

SRCS_CLEAN = \

SRCS = $(patsubst %, $(SOURCE_PATH)/%, $(SRCS_CLEAN))
OBJS = $(SRCS:$(SOURCE_PATH)/%.cpp=$(BUILD_PATH)/%.${OBJEXT})
EXECS = $(SRCS:$(SOURCE_PATH)/%.cpp=$(EXE_PATH)/%${EXE})

##
##	targets
##

ifeq ($(VERBOSE),NO)
.SILENT:
endif

all: ${EXECS}
	#@echo ${EXCES}

${EXECS} : $(EXE_PATH)/%${EXE} : $(BUILD_PATH)/%.${OBJEXT}
	@echo "Compiling" $@
	${CPP} ${DEF_TARGET} ${CPPFLAGS} $< ${LIBS}


${OBJS} : $(BUILD_PATH)/%.${OBJEXT}: $(SOURCE_PATH)/%.cpp
	@mkdir -p $(dir $@)
	${CPP} ${DEF_TARGET} -c ${IFLAGS} ${CPPFLAGS} $<

clean:
	${RM} -f ${OBJS} ${EXECS}

clobber: clean

