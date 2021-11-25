USE [LapTrinhMang]
GO
/****** Object:  Table [dbo].[DiemSinhVien]    Script Date: 25/11/2021 12:06:07 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[DiemSinhVien](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[hoten] [nvarchar](max) NULL,
	[mssv] [nchar](10) NULL,
	[toan] [real] NULL,
	[van] [real] NULL,
	[anh] [real] NULL,
 CONSTRAINT [PK_DiemSinhVien] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]

GO
SET IDENTITY_INSERT [dbo].[DiemSinhVien] ON 

INSERT [dbo].[DiemSinhVien] ([id], [hoten], [mssv], [toan], [van], [anh]) VALUES (4, N'Luong Van Sang', N'N18DCCN172', 10, 9.5, 9.8)
SET IDENTITY_INSERT [dbo].[DiemSinhVien] OFF
