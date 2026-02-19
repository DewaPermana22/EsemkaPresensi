using System;
using System.Collections.Generic;
using Microsoft.EntityFrameworkCore;

namespace LKS_ITSSA_2025.Models;

public partial class EsensiAppContext : DbContext
{
    public EsensiAppContext()
    {
    }

    public EsensiAppContext(DbContextOptions<EsensiAppContext> options)
        : base(options)
    {
    }

    public virtual DbSet<AbsenUser> AbsenUsers { get; set; }

    public virtual DbSet<Gaji> Gajis { get; set; }

    public virtual DbSet<KodeReveral> KodeReverals { get; set; }

    public virtual DbSet<Penggajian> Penggajians { get; set; }

    public virtual DbSet<StatusAbsen> StatusAbsens { get; set; }

    public virtual DbSet<Task> Tasks { get; set; }

    public virtual DbSet<TaskingProgress> TaskingProgresses { get; set; }

    public virtual DbSet<TodoTask> TodoTasks { get; set; }

    public virtual DbSet<User> Users { get; set; }

    protected override void OnConfiguring(DbContextOptionsBuilder optionsBuilder)
#warning To protect potentially sensitive information in your connection string, you should move it out of source code. You can avoid scaffolding the connection string by using the Name= syntax to read it from configuration - see https://go.microsoft.com/fwlink/?linkid=2131148. For more guidance on storing connection strings, see https://go.microsoft.com/fwlink/?LinkId=723263.
        => optionsBuilder.UseSqlServer("Data Source=DESKTOP-NJI58PS\\SQLEXPRESS02;Initial Catalog=EsensiApp;Integrated Security=True;Encrypt=True;Trust Server Certificate=True");

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        modelBuilder.Entity<AbsenUser>(entity =>
        {
            entity.ToTable("AbsenUser");

            entity.Property(e => e.Id).HasColumnName("ID");
            entity.Property(e => e.JamKeluar).HasColumnType("datetime");
            entity.Property(e => e.JamMasuk).HasColumnType("datetime");
            entity.Property(e => e.SelfieMasuk)
                .HasMaxLength(50)
                .IsUnicode(false);
            entity.Property(e => e.StatusId).HasColumnName("StatusID");
            entity.Property(e => e.UserId).HasColumnName("UserID");

            entity.HasOne(d => d.Status).WithMany(p => p.AbsenUsers)
                .HasForeignKey(d => d.StatusId)
                .HasConstraintName("FK_AbsenUser_StatusAbsen");

            entity.HasOne(d => d.User).WithMany(p => p.AbsenUsers)
                .HasForeignKey(d => d.UserId)
                .HasConstraintName("FK_AbsenUser_User");
        });

        modelBuilder.Entity<Gaji>(entity =>
        {
            entity.ToTable("Gaji");

            entity.Property(e => e.Id).HasColumnName("ID");
            entity.Property(e => e.Gaji1)
                .HasMaxLength(50)
                .IsUnicode(false)
                .HasColumnName("Gaji");
            entity.Property(e => e.UserId).HasColumnName("UserID");

            entity.HasOne(d => d.User).WithMany(p => p.Gajis)
                .HasForeignKey(d => d.UserId)
                .HasConstraintName("FK_Gaji_User");
        });

        modelBuilder.Entity<KodeReveral>(entity =>
        {
            entity.ToTable("KodeReveral");

            entity.Property(e => e.Id).HasColumnName("ID");
            entity.Property(e => e.Code)
                .HasMaxLength(10)
                .IsUnicode(false);
            entity.Property(e => e.UserId).HasColumnName("UserID");

            entity.HasOne(d => d.User).WithMany(p => p.KodeReverals)
                .HasForeignKey(d => d.UserId)
                .HasConstraintName("FK_KodeReveral_User");
        });

        modelBuilder.Entity<Penggajian>(entity =>
        {
            entity.ToTable("Penggajian");

            entity.Property(e => e.Id).HasColumnName("ID");
            entity.Property(e => e.Bonus)
                .HasMaxLength(50)
                .IsUnicode(false);
            entity.Property(e => e.GajiId).HasColumnName("GajiID");
            entity.Property(e => e.Pelanggaran)
                .HasMaxLength(50)
                .IsUnicode(false);
            entity.Property(e => e.Total)
                .HasMaxLength(50)
                .IsUnicode(false);
            entity.Property(e => e.UserId).HasColumnName("UserID");

            entity.HasOne(d => d.Gaji).WithMany(p => p.Penggajians)
                .HasForeignKey(d => d.GajiId)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("FK_Penggajian_Gaji");

            entity.HasOne(d => d.User).WithMany(p => p.Penggajians)
                .HasForeignKey(d => d.UserId)
                .HasConstraintName("FK_Penggajian_User");
        });

        modelBuilder.Entity<StatusAbsen>(entity =>
        {
            entity.ToTable("StatusAbsen");

            entity.Property(e => e.Id).HasColumnName("ID");
            entity.Property(e => e.Status)
                .HasMaxLength(50)
                .IsUnicode(false);
        });

        modelBuilder.Entity<Task>(entity =>
        {
            entity.ToTable("Task");

            entity.Property(e => e.Id).HasColumnName("ID");
            entity.Property(e => e.Task1)
                .HasColumnType("text")
                .HasColumnName("Task");
        });

        modelBuilder.Entity<TaskingProgress>(entity =>
        {
            entity.ToTable("TaskingProgress");

            entity.Property(e => e.Id).HasColumnName("ID");
            entity.Property(e => e.TaskId).HasColumnName("TaskID");
            entity.Property(e => e.UserId).HasColumnName("UserID");

            entity.HasOne(d => d.Task).WithMany(p => p.TaskingProgresses)
                .HasForeignKey(d => d.TaskId)
                .HasConstraintName("FK_TaskingProgress_Task");

            entity.HasOne(d => d.User).WithMany(p => p.TaskingProgresses)
                .HasForeignKey(d => d.UserId)
                .HasConstraintName("FK_TaskingProgress_User");
        });

        modelBuilder.Entity<TodoTask>(entity =>
        {
            entity.ToTable("TodoTask");

            entity.Property(e => e.Id).HasColumnName("ID");
            entity.Property(e => e.TaskId).HasColumnName("TaskID");
            entity.Property(e => e.TodoTask1)
                .HasColumnType("text")
                .HasColumnName("TodoTask");

            entity.HasOne(d => d.Task).WithMany(p => p.TodoTasks)
                .HasForeignKey(d => d.TaskId)
                .HasConstraintName("FK_TodoTask_Task");
        });

        modelBuilder.Entity<User>(entity =>
        {
            entity.ToTable("User");

            entity.Property(e => e.Id).HasColumnName("ID");
            entity.Property(e => e.Email)
                .HasMaxLength(50)
                .IsUnicode(false);
            entity.Property(e => e.EncryptBiometric).HasColumnType("text");
            entity.Property(e => e.Nama)
                .HasMaxLength(50)
                .IsUnicode(false);
            entity.Property(e => e.Password)
                .HasMaxLength(50)
                .IsUnicode(false);

            entity.HasOne(d => d.KodeReveralNavigation).WithMany(p => p.Users)
                .HasForeignKey(d => d.KodeReveral)
                .HasConstraintName("FK_User_KodeReveral");
        });

        OnModelCreatingPartial(modelBuilder);
    }

    partial void OnModelCreatingPartial(ModelBuilder modelBuilder);
}
